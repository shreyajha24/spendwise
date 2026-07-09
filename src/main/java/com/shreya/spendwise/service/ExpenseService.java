package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseResponse saveExpense(ExpenseRequest request) {
        Expense expense = new Expense(
                request.getAmount(),
                request.getCategory(),
                request.getDate(),
                request.getNote()
        );
        Expense savedExpense = expenseRepository.save(expense);
        return toResponse(savedExpense);
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        return toResponse(expense);
    }

    public List<ExpenseResponse> getExpensesByCategory(String category) {
        return expenseRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getNote()
        );
    }
}
