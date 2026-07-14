package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.UserRequest;
import com.shreya.spendwise.dto.UserResponse;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.EmailAlreadyExistsException;
import com.shreya.spendwise.exception.UserHasExpensesException;
import com.shreya.spendwise.exception.UserNotFoundException;
import com.shreya.spendwise.exception.UsernameAlreadyExistsException;
import com.shreya.spendwise.mapper.UserMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import com.shreya.spendwise.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       ExpenseRepository expenseRepository,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.userMapper = userMapper;
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUserById(id);
        
        // Check for duplicate username only if it has changed
        if (!user.getUsername().equals(request.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameAlreadyExistsException(request.getUsername());
            }
        }
        
        // Check for duplicate email only if it has changed
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
        }
        
        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        if (expenseRepository.existsByUser_Id(user.getId())) {
            throw new UserHasExpensesException(id);
        }
        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
