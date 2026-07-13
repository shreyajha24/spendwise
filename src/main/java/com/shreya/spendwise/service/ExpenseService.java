package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = expenseMapper.toEntity(request);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public List<ExpenseResponse> getExpenses(String category) {
        if (category != null && !category.isBlank()) {
            return getExpensesByCategory(category);
        }
        return getAllExpenses();
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        return expenseMapper.toResponse(findExpenseById(id));
    }

    public List<ExpenseResponse> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense expense = findExpenseById(id);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    public void deleteExpense(Long id) {
        Expense expense = findExpenseById(id);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }
}
