package com.example.resume.services;

import com.example.resume.dto.Request.GoogleAuthRequest;
import com.example.resume.dto.Request.LoginRequest;
import com.example.resume.dto.Request.RegisterRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.AuthResponse;
import com.example.resume.dto.UserInfo;
import com.example.resume.entity.User;
import com.example.resume.entity.UserMeta;
import com.example.resume.entity.UserProfileDetails;
import com.example.resume.entity.VerificationToken;
import com.example.resume.enums.Role;
import com.example.resume.repository.UserDetailRepository;
import com.example.resume.repository.UserRepository;
import com.example.resume.repository.VerificationTokenRepository;
import com.example.resume.utils.GoogleTokenVerifier;
import com.example.resume.utils.JwtUtil;
import com.example.resume.utils.UserMetaExtractor;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMetaExtractor userMetaExtractor;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;
    @Autowired
    private UserDetailRepository userDetailRepository;

    public ApiResponse<AuthResponse> register(RegisterRequest request){
        try {

            if (userService.existsByUsername(request.getUserName())) {
                return ApiResponse.<AuthResponse>builder()
                        .status("failed")
                        .message("Username already exists")
                        .data(null)
                        .build();
            }

            if (userService.existsByEmail(request.getEmail())) {
                return ApiResponse.<AuthResponse>builder()
                        .status("failed")
                        .message("Email already exists")
                        .data(null)
                        .build();
            }

            User newUser = new User();
            newUser.setUserName(request.getUserName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(Role.ADMIN);
            newUser.setVerified(false);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());

            userService.save(newUser);

            String accessToken = jwtUtil.generateAccessToken(newUser.getUserName());
            String refreshToken = jwtUtil.generateRefreshToken(newUser.getUserName());

            String token = UUID.randomUUID().toString();
            VerificationToken verifyToken = VerificationToken.builder()
                    .token(token)
                    .user(newUser)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();

            verificationTokenRepository.save(verifyToken);

            emailService.sendVerificationEmail(newUser.getEmail(), token);

            log.info("User registered successfully: {}", request.getUserName());

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .build();

            return ApiResponse.<AuthResponse>builder()
                    .status("success")
                    .message("Login Successful")
                    .data(authResponse)
                    .build();


        } catch (Exception e){
            log.error("Unexpected register error for user: {}", request.getEmail(), e);
            return ApiResponse.<AuthResponse>builder()
                    .status("failed")
                    .message("An unexpected error occurred. Please try again.")
                    .data(null)
                    .build();
        }
    }

    public ApiResponse<Void> verifyEmail(String token){
        try {
            // find the token first
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid verification token"));

            // check if token is expired
            if(verificationToken.getExpiresAt().isBefore(LocalDateTime.now())){
                verificationTokenRepository.delete(verificationToken);
                return ApiResponse.<Void>builder()
                        .status("failed")
                        .message("Verification link has expired. Please register again.")
                        .build();
            }

            // Mark user as verified
            User user = verificationToken.getUser();
            user.setVerified(true);
            userService.save(user);

            verificationTokenRepository.delete(verificationToken);

            log.info("Email verified successully for user: {}", user.getEmail());

            return ApiResponse.<Void>builder()
                    .status("success")
                    .message("Email verified successfully! You can now login.")
                    .build();

        } catch (Exception e){
            log.error("Email verification error", e);
            return ApiResponse.<Void>builder()
                    .status("failed")
                    .message("Invalid or expired verification token.")
                    .build();
        }
    }

    public ApiResponse<AuthResponse> login(LoginRequest request, HttpServletRequest httpServletRequest){


        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
            );

            User user =  userService.findByUsername(request.getUserName());

            if(!user.isVerified()){
                return ApiResponse.<AuthResponse>builder()
                        .status("failed")
                        .message("Please verify your email before logging in.")
                        .data(null)
                        .build();
            }

            String accessToken = jwtUtil.generateAccessToken(user.getUserName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());

            log.info("User Login successfully: {}", request.getUserName());

            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .user(userInfo)
                    .build();

            updateUserMeta(user, httpServletRequest);

            return ApiResponse.<AuthResponse>builder()
                    .status("success")
                    .message("Login Successful")
                    .data(authResponse)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected login error for user: {}", request.getUserName(), e);
            return ApiResponse.<AuthResponse>builder()
                    .status("failed")
                    .message("An unexpected error occurred. Please try again.")
                    .data(null)
                    .build();
        }
    }

    private void updateUserMeta(User user, HttpServletRequest request){
        UserProfileDetails userProfileDetails = userDetailRepository.findByUser(user);
        if(userProfileDetails == null) return;

        UserMeta userMeta = userMetaExtractor.extract(request);
        userProfileDetails.setUserMeta(userMeta);
        userDetailRepository.save(userProfileDetails);
        userProfileDetails.setUserIp(userMeta.getLastLoginIp());
        log.info("UserMeta updated for user: {} from IP: {}", user.getUserName(), userMeta.getLastLoginIp());
    }

    public void logout(){

    }

    public ApiResponse<AuthResponse> refreshToken(String token) {
        String username = jwtUtil.extractUsername(token);

        UserDetails user = userService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(token, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(900)
                .build();

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Token refreshed successfully")
                .data(authResponse)
                .build();
    }

    public ApiResponse<AuthResponse> googleLogin(GoogleAuthRequest request){
        try {
            GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.getIdToken());

            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            String googleId = payload.getSubject();
            String picture = (String) payload.get("picture");

            User user = userRepository.findByEmail(email).orElseGet(() -> {


                // Auto Generate unique username from email
                String baseUsername = email.split("@")[0];
                String username = generateUniqueUsername(baseUsername);

                User newUser = new User();
                newUser.setUserName(username);
                newUser.setEmail(email);
                newUser.setVerified(true);
                newUser.setGoogleId(googleId);
                newUser.setPassword(null);
                newUser.setRole(Role.USER);

               return userRepository.save(newUser);
            });

            // If existing user, link Google ID if not already linked
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }

            // Generate your own JWT
            String accessToken = jwtUtil.generateAccessToken(user.getUserName());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUserName());

            UserInfo userInfo = UserInfo.builder()
                    .id(user.getId())
                    .userName(user.getUserName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

            AuthResponse authResponse = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(900)
                    .user(userInfo)
                    .build();

            log.info("Google login successful for: {}", email);

            return ApiResponse.<AuthResponse>builder()
                    .status("success")
                    .message("Login successful")
                    .data(authResponse)
                    .build();


        } catch (Exception e){
            log.error("Google login error", e);
            return ApiResponse.<AuthResponse>builder()
                    .status("failed")
                    .message("Google login failed. Please try again.")
                    .data(null)
                    .build();
        }

    }

    private String generateUniqueUsername(String base) {
        String username = base;
        int counter = 1;
        while (userRepository.existsByUserName(username)) {
            username = base + counter++;
        }
        return username;
    }

}