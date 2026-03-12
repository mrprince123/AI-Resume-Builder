package com.example.resume.services;

import com.example.resume.dto.Request.LoginRequest;
import com.example.resume.dto.Request.RegisterRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.AuthResponse;
import com.example.resume.entity.User;
import com.example.resume.enums.Role;
import com.example.resume.repository.UserRepository;
import com.example.resume.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.authentication.AuthenticationManager;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public ApiResponse<AuthResponse> register(RegisterRequest request){
        try {

            // check if the user already exists
            // 1. Check if username already exists
            if (userService.existsByUsername(request.getFullName())) {
                return ApiResponse.<AuthResponse>builder()
                        .status("failed")
                        .message("Username already exists")
                        .data(null)
                        .build();
            }

            // 2. Check if email already exists
            if (userService.existsByEmail(request.getEmail())) {
                return ApiResponse.<AuthResponse>builder()
                        .status("failed")
                        .message("Email already exists")
                        .data(null)
                        .build();
            }

            // if not create send verification link on email - future

            // if verify then save data to the database
            User newUser = new User();
            newUser.setUserName(request.getFullName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRole(Role.ADMIN);

            userService.save(newUser);

            // return the response
            String accessToken = jwtUtil.generateAccessToken(newUser.getUserName());
            String refreshToken = jwtUtil.generateRefreshToken(newUser.getUserName());

            log.info("User registered successfully: {}", request.getFullName());

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

    public ApiResponse<AuthResponse> login(LoginRequest request){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails user =  userService.loadUserByUsername(request.getUsername());

            String accessToken = jwtUtil.generateAccessToken(user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            log.info("User Login successfully: {}", request.getUsername());

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

        } catch (Exception e) {
            log.error("Unexpected login error for user: {}", request.getUsername(), e);
            return ApiResponse.<AuthResponse>builder()
                    .status("failed")
                    .message("An unexpected error occurred. Please try again.")
                    .data(null)
                    .build();
        }
    }


    public void logout(){

    }

    public ApiResponse<AuthResponse> refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        UserDetails user = userService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtUtil.generateAccessToken(username);   // ✅ Fixed
        String newRefreshToken = jwtUtil.generateRefreshToken(username); // ✅ Rotated

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) // ✅ Return new refresh token
                .tokenType("Bearer")
                .expiresIn(900)
                .build();

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Token refreshed successfully")
                .data(authResponse)
                .build();
    }



}