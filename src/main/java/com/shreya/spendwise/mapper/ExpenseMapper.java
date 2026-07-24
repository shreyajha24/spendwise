package com.shreya.spendwise.mapper;

import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setNote(request.getNote());
        return expense;
    }

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getNote()
        );
    }
    public void updateEntity(ExpenseRequest request, Expense expense) {
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setNote(request.getNote());
    }
}
