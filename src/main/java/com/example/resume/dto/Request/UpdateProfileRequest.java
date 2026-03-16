package com.example.resume.dto.Request;

import com.example.resume.entity.UserMeta;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String bio;
    private String phoneNumber;
    private String location;
    private UserMeta userMeta;
}