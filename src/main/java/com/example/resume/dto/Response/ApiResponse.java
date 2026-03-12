package com.example.resume.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
}


//{
//        "status": "success",
//        "message": "Login successful",
//        "data": {
//            "access_token": "...",
//            "refresh_token": "...",
//            "token_type": "Bearer",
//            "expires_in": 900
//        }
//}