package com.example.resume.controller;

import com.example.resume.dto.Request.LoginRequest;
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
    private ResponseEntity<String> register(@RequestBody RegisterRequest request){
        String token =  authService.register(request);
        return ResponseEntity.ok("Token" + token);
    }

    @PostMapping("/login")
    private ResponseEntity<String> login(@RequestBody LoginRequest request){
            String token =  authService.login(request);
            return ResponseEntity.ok("Token" + token);
    }

    @GetMapping("/test")
    private ResponseEntity<String> test(){
        return ResponseEntity.ok("Hello world Sniper");
    }
}
