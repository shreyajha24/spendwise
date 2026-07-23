package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import com.shreya.spendwise.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final SecurityUtils securityUtils;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper,
            SecurityUtils securityUtils) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.securityUtils = securityUtils;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Expense expense = expenseMapper.toEntity(request, currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public List<ExpenseResponse> getExpenses(String category) {
        Long currentUserId = securityUtils.getCurrentUser().getId();
        if (category != null && !category.isBlank()) {
            return getExpensesByCategory(currentUserId, category);
        }
        return getAllExpenses(currentUserId);
    }

    public List<ExpenseResponse> getAllExpenses(Long userId) {
        return expenseRepository.findByUser_Id(userId).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        Long currentUserId = securityUtils.getCurrentUser().getId();
        return expenseMapper.toResponse(findExpenseById(id, currentUserId));
    }

    public List<ExpenseResponse> getExpensesByCategory(Long userId, String category) {
        return expenseRepository.findByUser_IdAndCategory(userId, category).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long currentUserId = securityUtils.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    public void deleteExpense(Long id) {
        Long currentUserId = securityUtils.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }
}
