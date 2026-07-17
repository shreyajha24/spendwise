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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ExpenseRepository expenseRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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
        
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        
        userMapper.updateEntity(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        if (expenseRepository.existsByUser_Id(id)) {
            throw new UserHasExpensesException(id);
        }
        userRepository.delete(user);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
