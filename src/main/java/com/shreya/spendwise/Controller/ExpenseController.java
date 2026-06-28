package com.shreya.spendwise.Controller;

import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.repository.ExpenseRepository;
import org.springframework.web.bind.annotation.*;


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
    @GetMapping("/students")
    public String getStudents(
            @RequestParam String branch
    ){
        return "Branch = " + branch;
    }
    @PostMapping("/expenses")
    public Expense addExpense(
            @RequestBody Expense expenses
    ){
        return expenses;
    }
}
