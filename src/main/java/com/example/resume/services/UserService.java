package com.example.resume.services;

import com.example.resume.dto.Request.ChangePasswordRequest;
import com.example.resume.dto.Request.UpdateProfileRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.entity.User;
import com.example.resume.entity.UserProfileDetails;
import com.example.resume.enums.Status;
import com.example.resume.repository.UserDetailRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.resume.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username);

        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUserName())
                    .password(user.getPassword())
                    .roles(String.valueOf(user.getRole()))
                    .build();
        }

        throw new UsernameNotFoundException("User not found with username" + username);
    }

    public User save(User user) {
        log.info("Saving user: {}", user.getEmail());
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUserName(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    //    1. Update Profile Function
    @Transactional
    public UserProfileDetails updateProfile(String username, UpdateProfileRequest request) {
        // 1. Find the user
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 2. Find the user details
        UserProfileDetails userDetails = userDetailRepository.findByUser(user);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User Details not found: " + username);
        }

        // 3. Update only if the field is provided (partial update)
        if (request.getFirstName() != null)   userDetails.setFirstName(request.getFirstName());
        if (request.getLastName() != null)    userDetails.setLastName(request.getLastName());
        if (request.getBio() != null)         userDetails.setBio(request.getBio());
        if (request.getPhoneNumber() != null) userDetails.setPhoneNumber(request.getPhoneNumber());
        if (request.getLocation() != null)    userDetails.setLocation(request.getLocation());

        // 4. Save and return
        userDetailRepository.save(userDetails);
        log.info("Profile updated successfully for user: {}", username);

        return userDetails;

    }

    public User updateAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found " + username);
        }

        // 2. find the user details
        UserProfileDetails userDetails = userDetailRepository.findByUser(user);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User Details not found " + username);
        }

        userDetails.setAvatar(avatarUrl);
        log.info("User Avatar Updated Successfully for user id: {}", user.getId());
        userDetailRepository.save(userDetails);

        return user;

    }

    public void softDeleteProfile(String username) {
        // 1. find the user
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found " + username);
        }

        // 2. find the user details
        UserProfileDetails userDetails = userDetailRepository.findByUser(user);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User Details not found " + username);
        }

        // 3. Anonymize user table
        user.setUserName("deleted_" + user.getId());             // can't be null (nullable = false)
        user.setEmail("deleted_" + user.getId() + "@removed.com"); // fixed typo "deleteed"
        user.setPassword(null);                                  // remove credentials
        user.setStatus(Status.INACTIVE);

        // 4. Anonymize user_details table
        userDetails.setFirstName("Deleted");
        userDetails.setLastName("User");
        userDetails.setAvatar(null);
        userDetails.setBio(null);
        userDetails.setPhoneNumber(null);
        userDetails.setUserIp(null);


        userRepository.save(user);
        userDetailRepository.save(userDetails);

        log.info("Account soft-deleted for user id: {}", user.getId());

    }

    public UserProfileDetails getUserProfile(String username) {
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserProfileDetails userDetails = userDetailRepository.findByUser(user);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User Details not found: " + username);
        }

        return userDetails;

    }

    public User changePassword(String username, ChangePasswordRequest request){
        // Find the User
        User user = userRepository.findByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Compare hashed passwords
        if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        log.info("User Password Changed Successfully for user id: {}", user.getId());
        userRepository.save(user);

        return user;
    }


}
