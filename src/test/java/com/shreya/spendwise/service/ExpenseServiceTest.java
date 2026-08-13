package com.shreya.spendwise.service;

import com.shreya.spendwise.dto.ExpenseFilterRequest;
import com.shreya.spendwise.dto.ExpensePageResponse;
import com.shreya.spendwise.dto.ExpenseResponse;
import com.shreya.spendwise.dto.QuickExpenseTemplateResponse;
import com.shreya.spendwise.dto.WeeklyInsightResponse;
import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import com.shreya.spendwise.exception.ExpenseNotFoundException;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Test
    void shouldReturnQuickTemplates() {
        List<QuickExpenseTemplateResponse> templates = expenseService.getQuickExpenseTemplates();

        assertFalse(templates.isEmpty());
        assertTrue(templates.stream().anyMatch(template -> "bus".equals(template.getTemplateKey())));
        assertTrue(templates.stream().anyMatch(template -> "coffee".equals(template.getTemplateKey())));
    }

    @Test
    void shouldCreateExpenseFromTemplate() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(expenseMapper.toResponse(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            return new ExpenseResponse(
                    expense.getId(),
                    expense.getAmount(),
                    expense.getCategory(),
                    expense.getDate(),
                    expense.getNote()
            );
        });

        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense expense = invocation.getArgument(0);
            expense.setId(999L);
            return expense;
        });

        ExpenseResponse response = expenseService.createExpenseFromTemplate("bus", LocalDate.now());

        assertEquals(999L, response.getId());
        assertEquals(Category.TRANSPORT, response.getCategory());
        assertEquals(60.0, response.getAmount());
        assertEquals("Bus fare", response.getNote());
    }

    @Test
    void shouldBuildWeeklyInsights() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);

        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        Expense foodExpense = new Expense(400.0, Category.FOOD, weekStart.plusDays(1), "Lunch");
        foodExpense.setUser(testUser);
        Expense transportExpense = new Expense(120.0, Category.TRANSPORT, weekStart.plusDays(2), "Bus");
        transportExpense.setUser(testUser);

        when(expenseRepository.findByUser_IdAndDateBetween(eq(testUser.getId()), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(foodExpense, transportExpense));

        WeeklyInsightResponse insight = expenseService.getWeeklyInsights();

        assertEquals(520.0, insight.getTotalSpent());
        assertEquals(2L, insight.getTotalTransactions());
        assertEquals(Category.FOOD, insight.getTopCategory());
        assertNotNull(insight.getSummary());
        assertFalse(insight.getCategoryBreakdown().isEmpty());
    }

    @Test
    void shouldGetExpenseById() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        Expense expense = new Expense();
        expense.setId(25L);
        expense.setAmount(100.0);
        expense.setUser(testUser);

        when(expenseRepository.findByIdAndUser_Id(25L, testUser.getId()))
                .thenReturn(Optional.of(expense));
        when(expenseMapper.toResponse(expense))
                .thenReturn(new ExpenseResponse(25L, 100.0, Category.FOOD, LocalDate.now(), "Test"));

        ExpenseResponse result = expenseService.getExpenseById(25L);

        assertNotNull(result);
        assertEquals(25L, result.getId());
        assertEquals(100.0, result.getAmount());
        verify(expenseRepository, times(1)).findByIdAndUser_Id(25L, testUser.getId());
    }

    @Test
    void shouldThrowExceptionWhenExpenseNotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(expenseRepository.findByIdAndUser_Id(99L, testUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ExpenseNotFoundException.class, () -> expenseService.getExpenseById(99L));
        verify(expenseRepository, times(1)).findByIdAndUser_Id(99L, testUser.getId());
    }
}
