package com.shreya.spendwise.controller;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.dto.QuickExpenseTemplateResponse;
import com.shreya.spendwise.dto.WeeklyInsightResponse;
import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.exception.ErrorResponse;
import com.shreya.spendwise.exception.ValidationErrorResponse;
import com.shreya.spendwise.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Expenses", description = "Expense management endpoints for the authenticated user")
@SecurityRequirement(name = "bearer-key")
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
    @Operation(
            summary = "Get expense by ID",
            description = "Retrieves a specific expense belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpenseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Expense not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @Operation(summary = "List expenses", description = "Returns a paginated list of expenses belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully", content = @Content(schema = @Schema(implementation = ExpensePageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ExpensePageResponse> getExpenses(@ModelAttribute ExpenseFilterRequest filterRequest) {
        return ResponseEntity.ok(expenseService.getExpenses(filterRequest));
    }

    @Operation(summary = "List quick expense templates", description = "Returns the available templates for creating expenses quickly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully", content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuickExpenseTemplateResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/templates")
    public ResponseEntity<List<QuickExpenseTemplateResponse>> getQuickExpenseTemplates() {
        return ResponseEntity.ok(expenseService.getQuickExpenseTemplates());
    }
    @Operation(
            summary = "Create an expense from a template",
            description = "Creates an expense for the currently authenticated user using a quick expense template."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpenseResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid expense data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/templates/{templateKey}")
    public ResponseEntity<ExpenseResponse> createExpenseFromTemplate(
            @PathVariable String templateKey,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpenseFromTemplate(templateKey, date));
    }

    @Operation(summary = "Get weekly spending insights", description = "Returns weekly spending insights for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weekly insights retrieved successfully", content = @Content(schema = @Schema(implementation = WeeklyInsightResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/insights/weekly")
    public ResponseEntity<WeeklyInsightResponse> getWeeklyInsights() {
        return ResponseEntity.ok(expenseService.getWeeklyInsights());
    }

    @Operation(summary = "Create an expense", description = "Creates an expense for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense created successfully", content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(oneOf = {
                            ValidationErrorResponse.class,
                            ErrorResponse.class
                    }))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }

    @Operation(summary = "Update an expense", description = "Updates an expense belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense updated successfully", content = @Content(schema = @Schema(implementation = ExpenseResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(oneOf = {
                            ValidationErrorResponse.class,
                            ErrorResponse.class
                    }))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @Operation(summary = "Delete an expense", description = "Deletes an expense belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Expense not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
