package com.shreya.spendwise.Controller;

import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ExpenseController {
    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/expenses/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(
                expenseService.getExpenseById(id)
        );
    }

    @GetMapping("/expenses")
    public List<Expense> getExpenses(
            @RequestParam(required = false) String category
    ) {
        if (category != null) {
            return expenseService.getExpensesByCategory(category);
        }

        return expenseService.getAllExpenses();
    }

    @PostMapping("/expenses")
    public Expense addExpense(
            @RequestBody Expense expenses
    ){
        return expenseService.saveExpense(expenses);
    }
}
