package com.example.resume.controller;

import com.example.resume.dto.Request.GoogleAuthRequest;
import com.example.resume.dto.Request.LoginRequest;
import com.example.resume.dto.Request.RegisterRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.AuthResponse;
import com.example.resume.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return authService.login(loginRequest, request);
    }

    @PostMapping("/{username}/logout")
    public ApiResponse<Void> logout(@PathVariable String username, HttpServletRequest request) {
        return authService.logout(username, request);
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refreshToken(@RequestBody String token) {
        return authService.refreshToken(token);
    }

    @PostMapping("/google")
    public ApiResponse<AuthResponse> googleLogin(@RequestBody GoogleAuthRequest request) {
        return authService.googleLogin(request);
    }

    @GetMapping("/test")
    private ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello world Sniper");
    }
}
