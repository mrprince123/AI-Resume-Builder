package com.example.resume.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMeta {
    private String device;
    private String timezone;
    private String lastLoginIp;
    private String browser;
    private String os;
}
