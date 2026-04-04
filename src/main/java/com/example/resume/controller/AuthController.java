package com.example.resume.controller;

import com.example.resume.dto.Request.GoogleAuthRequest;
import com.example.resume.dto.Request.LoginRequest;
import com.example.resume.dto.Request.RegisterRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.AuthResponse;
import com.example.resume.services.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Endpoints for managing all auth functions")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Login Successful")
                .data(authResponse)
                .build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse authResponse = authService.login(loginRequest, request);

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Login Successful")
                .data(authResponse)
                .build();
    }

    @PostMapping("/{username}/logout")
    public ApiResponse<Void> logout(@PathVariable String username, HttpServletRequest request) {
        authService.logout(username, request);

        return ApiResponse.<Void>builder()
                .status("success")
                .message("Logged out successfully")
                .data(null)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestBody String token) {
        AuthResponse authResponse = authService.refreshToken(token);

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Token refreshed successfully")
                .data(authResponse)
                .build();
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
        AuthResponse authResponse = authService.googleLogin(request);

        return ApiResponse.<AuthResponse>builder()
                .status("success")
                .message("Login successful")
                .data(authResponse)
                .build();
    }

    @GetMapping("/test")
    private ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello world Sniper");
    }


}
