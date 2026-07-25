package com.shreya.spendwise.service;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.dto.ExpenseRequest;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.shreya.spendwise.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = getCurrentUser();
        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public ExpensePageResponse getExpenses(
            Category category, String note, int page, int size, String sort) {
        validatePagination(page, size);
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        String trimmedNote = note == null ? "" : note.trim();
        boolean hasNote = !trimmedNote.isEmpty();

        Page<Expense> expensePage;
        if (category != null && hasNote) {
            expensePage = expenseRepository.findByUserAndCategoryAndNoteContainingIgnoreCase(
                    currentUser, category, trimmedNote, pageable);
        } else if (category != null) {
            expensePage = expenseRepository.findByUserAndCategory(currentUser, category, pageable);
        } else if (hasNote) {
            expensePage = expenseRepository.findByUserAndNoteContainingIgnoreCase(
                    currentUser, trimmedNote, pageable);
        } else {
            expensePage = expenseRepository.findByUser(currentUser, pageable);
        }

        return new ExpensePageResponse(
                expensePage.getContent().stream().map(expenseMapper::toResponse).toList(),
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements(),
                expensePage.getTotalPages(),
                expensePage.isFirst(),
                expensePage.isLast()
        );
    }

    public List<ExpenseResponse> getAllExpenses() {
        User currentUser = getCurrentUser();
        return expenseRepository.findByUser(currentUser).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return expenseMapper.toResponse(findExpenseById(id, currentUserId));
    }

    public List<ExpenseResponse> getExpensesByCategory(Long userId, Category category) {
        return expenseRepository.findByUser_IdAndCategory(userId, category).stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long currentUserId = getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    public void deleteExpense(Long id) {
        Long currentUserId = getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || "date-desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "date").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("date-asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "date").and(Sort.by(Sort.Direction.ASC, "id"));
        }
        if ("amount-desc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "amount").and(Sort.by(Sort.Direction.DESC, "id"));
        }
        if ("amount-asc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "amount").and(Sort.by(Sort.Direction.ASC, "id"));
        }
        throw new IllegalArgumentException("Invalid sort value.");
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100.");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }
        throw new IllegalStateException("No authenticated user found");
    }

}
