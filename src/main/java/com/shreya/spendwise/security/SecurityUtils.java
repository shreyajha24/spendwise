package com.shreya.spendwise.security;

import com.shreya.spendwise.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw new IllegalStateException("No authenticated user found");
    }
}
