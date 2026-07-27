package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.mapper.ExpenseMapper;
import com.shreya.spendwise.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void shouldGetExpensesWithFilter() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        Expense expense = new Expense();
        expense.setId(10L);
        expense.setCategory(Category.FOOD);
        expense.setUser(testUser);

        when(expenseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(expense)));

        ExpenseFilterRequest filter = new ExpenseFilterRequest();
        filter.setCategory(Category.FOOD);
        filter.setNote("lunch");
        filter.setSortBy("amount");

        ExpensePageResponse response = expenseService.getExpenses(filter);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(expenseRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldThrowExceptionForInvalidSortBy() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        ExpenseFilterRequest filter = new ExpenseFilterRequest();
        filter.setSortBy("invalidField");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> expenseService.getExpenses(filter));

        assertTrue(ex.getMessage().contains("Invalid sortBy value"));
    }
}
