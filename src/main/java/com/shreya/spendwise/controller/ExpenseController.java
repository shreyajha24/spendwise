package com.shreya.spendwise.controller;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.dto.QuickExpenseTemplateResponse;
import com.shreya.spendwise.dto.WeeklyInsightResponse;
import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Category.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (text == null || text.isBlank()) {
                    setValue(null);
                } else {
                    try {
                        setValue(Category.valueOf(text.trim().toUpperCase()));
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException("Invalid category value: '" + text + "'.");
                    }
                }
            }
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping
    public ResponseEntity<ExpensePageResponse> getExpenses(@ModelAttribute ExpenseFilterRequest filterRequest) {
        return ResponseEntity.ok(expenseService.getExpenses(filterRequest));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<QuickExpenseTemplateResponse>> getQuickExpenseTemplates() {
        return ResponseEntity.ok(expenseService.getQuickExpenseTemplates());
    }

    @PostMapping("/templates/{templateKey}")
    public ResponseEntity<ExpenseResponse> createExpenseFromTemplate(
            @PathVariable String templateKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpenseFromTemplate(templateKey, date));
    }

    @GetMapping("/insights/weekly")
    public ResponseEntity<WeeklyInsightResponse> getWeeklyInsights() {
        return ResponseEntity.ok(expenseService.getWeeklyInsights());
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
