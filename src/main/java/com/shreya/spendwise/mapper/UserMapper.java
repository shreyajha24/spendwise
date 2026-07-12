package com.shreya.spendwise.mapper;

import com.shreya.spendwise.dto.UserRequest;
import com.shreya.spendwise.dto.UserResponse;
import com.shreya.spendwise.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        return new User(request.getUsername(), request.getEmail());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    public void updateEntity(UserRequest request, User user) {
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
    }
}
