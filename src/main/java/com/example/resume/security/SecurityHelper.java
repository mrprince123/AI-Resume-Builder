package com.example.resume.security;

import com.example.resume.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// SecurityHelper.java
@Component
public class SecurityHelper {

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        return (User) authentication.getPrincipal();
    }

    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }
}
