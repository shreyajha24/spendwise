package com.shreya.spendwise.service;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import com.shreya.spendwise.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = getCurrentUser();
        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public List<ExpenseResponse> getExpenses(Category category) {
        User currentUser = getCurrentUser();
        if (category != null) {
            return getExpensesByCategory(currentUser.getId(), category);
        }
        return getAllExpenses();
    }

    public List<ExpenseResponse> getAllExpenses() {
        User currentUser = getCurrentUser();
        return expenseRepository.findByUser(currentUser).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return expenseMapper.toResponse(findExpenseById(id, currentUserId));
    }

    public List<ExpenseResponse> getExpensesByCategory(Long userId, Category category) {
        return expenseRepository.findByUser_IdAndCategory(userId, category).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long currentUserId = getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    public void deleteExpense(Long id) {
        Long currentUserId = getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw new IllegalStateException("No authenticated user found");
    }

}
