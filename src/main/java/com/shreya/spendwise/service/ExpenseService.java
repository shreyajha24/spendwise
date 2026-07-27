package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import static com.shreya.spendwise.repository.specification.ExpenseSpecification.hasCategory;
import static com.shreya.spendwise.repository.specification.ExpenseSpecification.hasNote;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final CurrentUserService currentUserService;

    public ExpenseService(
            ExpenseRepository expenseRepository,
            ExpenseMapper expenseMapper,
            CurrentUserService currentUserService) {
        this.expenseRepository = expenseRepository;
        this.expenseMapper = expenseMapper;
        this.currentUserService = currentUserService;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(currentUser);
        Expense savedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(savedExpense);
    }

    public ExpensePageResponse getExpenses(ExpenseFilterRequest filterRequest) {
        validatePagination(filterRequest.getPage(), filterRequest.getSize());
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(
                filterRequest.getPage(),
                filterRequest.getSize(),
                resolveSort(filterRequest.getSortBy(), filterRequest.getDirection())
        );

        String note = filterRequest.getNote();
        boolean hasNoteFilter = note != null && !note.isBlank();
        if (hasNoteFilter) {
            note = note.trim();
        }

        Specification<Expense> spec = Specification.where((Specification<Expense>) null)
                .and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), currentUser));

        if (filterRequest.getCategory() != null) {
            spec = spec.and(hasCategory(filterRequest.getCategory()));
        }

        if (hasNoteFilter) {
            spec = spec.and(hasNote(note));
        }

        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

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

    public ExpenseResponse getExpenseById(Long id) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        return expenseMapper.toResponse(findExpenseById(id, currentUserId));
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseMapper.updateEntity(request, expense);
        Expense updatedExpense = expenseRepository.save(expense);
        return expenseMapper.toResponse(updatedExpense);
    }

    public void deleteExpense(Long id) {
        Long currentUserId = currentUserService.getCurrentUser().getId();
        Expense expense = findExpenseById(id, currentUserId);
        expenseRepository.delete(expense);
    }

    private Expense findExpenseById(Long id, Long userId) {
        return expenseRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private Sort resolveSort(String sortBy, String direction) {
        String normalizedSortBy = sortBy == null ? "date" : sortBy.trim().toLowerCase();
        String normalizedDirection = direction == null ? "desc" : direction.trim().toLowerCase();

        if (!"asc".equals(normalizedDirection) && !"desc".equals(normalizedDirection)) {
            throw new IllegalArgumentException("Invalid direction value.");
        }

        Sort.Direction sortDirection = "asc".equals(normalizedDirection)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        if ("date".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "date").and(Sort.by(sortDirection, "id"));
        }
        if ("amount".equals(normalizedSortBy)) {
            return Sort.by(sortDirection, "amount").and(Sort.by(sortDirection, "id"));
        }

        throw new IllegalArgumentException("Invalid sortBy value.");
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100.");
        }
    }

}
