package com.example.resume.dto;

import com.example.resume.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private Long id;
    private String userName;
    private String email;
    private Role role;
}
