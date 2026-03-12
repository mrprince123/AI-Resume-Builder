package com.example.resume.controller;

import com.example.resume.dto.Request.LoginRequest;
import com.example.resume.dto.Request.RegisterRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.AuthResponse;
import com.example.resume.services.AuthService;
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

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/test")
    private ResponseEntity<String> test(){
        return ResponseEntity.ok("Hello world Sniper");
    }
}
