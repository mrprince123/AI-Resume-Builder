package com.example.resume.dto.Request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
}
