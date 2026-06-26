package com.shreya.spendwise.Controller;

import com.shreya.spendwise.repository.ExpenseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ExpenseController {
    private final ExpenseRepository expenseRepository;
    public ExpenseController(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }
    @GetMapping("/expenses/{id}")
    public String getExpenses(
            @PathVariable Long id
    ){
        return "Expense Id = "+id;
    }
    @GetMapping("/expenses")
    public String getExpenseByCategory(
            @RequestParam String category
    ){
        return "Category = " + category;
    }
}
