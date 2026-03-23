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
        return userService.updateProfile(username, request);
    }

    // PUT /api/v1/user/{username}/avatar
    @PutMapping("/{username}/avatar")
    public ApiResponse<User> updateUserAvatar(
            @PathVariable String username,
            @RequestBody String avatarUrl) {
        return userService.updateAvatar(username, avatarUrl);
    }

    // DELETE /api/v1/user/{username}
    @DeleteMapping("/{username}")
    public ApiResponse<Void> softDeleteUserProfile(
            @PathVariable String username) {
        return userService.softDeleteProfile(username);
    }

    // GET /api/v1/user/{username}/profile
    @GetMapping("/{username}/profile")
    public ApiResponse<UserProfileDetails> getUserProfile(@PathVariable String username) {
        return userService.getUserProfile(username);
    }

    @PostMapping("/{username}/changePassword")
    public ApiResponse<User> changePassword(@PathVariable String username, @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(username, request);
    }

}
