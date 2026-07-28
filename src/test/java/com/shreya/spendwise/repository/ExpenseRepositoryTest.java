package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ExpenseRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("john_doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");
        entityManager.persist(testUser);
        entityManager.flush();

        Expense e1 = new Expense();
        e1.setAmount(100.0);
        e1.setCategory(Category.FOOD);
        e1.setDate(LocalDate.of(2026, 1, 15));
        e1.setNote("Groceries");
        e1.setUser(testUser);
        entityManager.persist(e1);

        Expense e2 = new Expense();
        e2.setAmount(200.0);
        e2.setCategory(Category.FOOD);
        e2.setDate(LocalDate.of(2026, 1, 20));
        e2.setNote("Dinner");
        e2.setUser(testUser);
        entityManager.persist(e2);

        Expense e3 = new Expense();
        e3.setAmount(300.0);
        e3.setCategory(Category.TRANSPORT);
        e3.setDate(LocalDate.of(2026, 2, 10));
        e3.setNote("Fuel");
        e3.setUser(testUser);
        entityManager.persist(e3);

        entityManager.flush();
    }

    @Test
    void shouldCalculateTotalSpendingOfCurrentUser() {
        Double totalSpending = expenseRepository.findTotalSpendingByUserId(testUser.getId());
        assertEquals(600.0, totalSpending);
    }

    @Test
    void shouldCalculateTotalNumberOfExpenses() {
        Long count = expenseRepository.countTotalExpensesByUserId(testUser.getId());
        assertEquals(3L, count);
    }

    @Test
    void shouldFindHighestExpense() {
        Double highestExpense = expenseRepository.findHighestExpenseByUserId(testUser.getId());
        assertEquals(300.0, highestExpense);
    }

    @Test
    void shouldFindAverageExpense() {
        Double averageExpense = expenseRepository.findAverageExpenseByUserId(testUser.getId());
        assertEquals(200.0, averageExpense);
    }

    @Test
    void shouldFindCategoryWiseSpending() {
        List<Object[]> categorySpending = expenseRepository.findCategoryWiseSpendingByUserId(testUser.getId());
        assertNotNull(categorySpending);
        assertEquals(2, categorySpending.size());

        for (Object[] row : categorySpending) {
            Category category = (Category) row[0];
            Double sum = (Double) row[1];

            if (category == Category.FOOD) {
                assertEquals(300.0, sum);
            } else if (category == Category.TRANSPORT) {
                assertEquals(300.0, sum);
            } else {
                fail("Unexpected category: " + category);
            }
        }
    }

    @Test
    void shouldFindMonthlySpending() {
        List<Object[]> monthlySpending = expenseRepository.findMonthlySpendingByUserId(testUser.getId());
        assertNotNull(monthlySpending);
        assertEquals(2, monthlySpending.size());

        // First result should be Feb 2026 due to DESC order
        Object[] febRow = monthlySpending.get(0);
        assertEquals(2026, ((Number) febRow[0]).intValue());
        assertEquals(2, ((Number) febRow[1]).intValue());
        assertEquals(300.0, ((Number) febRow[2]).doubleValue());

        // Second result should be Jan 2026
        Object[] janRow = monthlySpending.get(1);
        assertEquals(2026, ((Number) janRow[0]).intValue());
        assertEquals(1, ((Number) janRow[1]).intValue());
        assertEquals(300.0, ((Number) janRow[2]).doubleValue());
    }
}
