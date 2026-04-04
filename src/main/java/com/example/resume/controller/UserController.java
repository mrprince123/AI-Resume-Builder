package com.example.resume.controller;

import com.example.resume.dto.Request.ChangePasswordRequest;
import com.example.resume.dto.Request.UpdateProfileRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.entity.User;
import com.example.resume.entity.UserProfileDetails;
import com.example.resume.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Endpoints for managing users functions")

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private UserService userService;

    // PUT /api/v1/user/{username}/profile
    @PutMapping("/{username}/profile")
    public ApiResponse<UserProfileDetails> updateUserProfile(
            @PathVariable String username,
            @RequestBody UpdateProfileRequest request) {
        UserProfileDetails userProfileDetails = userService.updateProfile(username, request);

        return ApiResponse.<UserProfileDetails>builder()
                .status("success")
                .message("Profile Updated Successfully")
                .data(userProfileDetails)
                .build();
    }

    // PUT /api/v1/user/{username}/avatar
    @PutMapping("/{username}/avatar")
    public ApiResponse<User> updateUserAvatar(
            @PathVariable String username,
            @RequestBody String avatarUrl) {
        User user = userService.updateAvatar(username, avatarUrl);

        return ApiResponse.<User>builder()
                .status("success")
                .message("User Avatar Updated")
                .data(user)
                .build();
    }

    // DELETE /api/v1/user/{username}
    @DeleteMapping("/{username}")
    public ApiResponse<Void> softDeleteUserProfile(
            @PathVariable String username) {
        userService.softDeleteProfile(username);

        return ApiResponse.<Void>builder()
                .status("success")
                .message("Account deleted successfully")
                .data(null)
                .build();
    }

    // GET /api/v1/user/{username}/profile
    @GetMapping("/{username}/profile")
    public ApiResponse<UserProfileDetails> getUserProfile(@PathVariable String username) {
        UserProfileDetails userProfileDetails = userService.getUserProfile(username);

        return ApiResponse.<UserProfileDetails>builder()
                .status("success")
                .message("User Profile Fetched Successfully")
                .data(userProfileDetails)
                .build();
    }

    @PostMapping("/{username}/changePassword")
    public ApiResponse<User> changePassword(@PathVariable String username, @RequestBody ChangePasswordRequest request) {
        User user = userService.changePassword(username, request);

        return ApiResponse.<User>builder()
                .status("success")
                .message("Password changed successfully")
                .data(user)
                .build();
    }

}
