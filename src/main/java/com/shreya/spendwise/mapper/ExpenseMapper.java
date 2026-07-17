package com.shreya.spendwise.mapper;

import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public Expense toEntity(ExpenseRequest request) {
        return new Expense(
                request.getAmount(),
                request.getCategory(),
                request.getDate(),
                request.getNote()
        );
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
