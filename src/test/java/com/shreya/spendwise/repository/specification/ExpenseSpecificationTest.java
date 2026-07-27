package com.shreya.spendwise.repository.specification;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseSpecificationTest {

    @Test
    void shouldCreateByFilterSpecificationWithUserOnly() {
        User user = new User();
        user.setId(1L);

        Specification<Expense> spec = ExpenseSpecifications.byFilter(user, null, null);

        assertNotNull(spec);
    }

    @Test
    void shouldCreateByFilterSpecificationWithCategoryAndNote() {
        User user = new User();
        user.setId(1L);

        Specification<Expense> spec = ExpenseSpecifications.byFilter(user, Category.FOOD, "lunch");

        assertNotNull(spec);
    }

    @Test
    void shouldDelegateInExpenseSpecification() {
        User user = new User();
        user.setId(1L);

        assertNotNull(ExpenseSpecification.belongsToUser(user));
        assertNotNull(ExpenseSpecification.hasCategory(Category.FOOD));
        assertNotNull(ExpenseSpecification.hasNote("test"));
    }
}
