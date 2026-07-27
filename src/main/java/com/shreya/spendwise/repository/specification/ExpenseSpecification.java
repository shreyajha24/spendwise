package com.shreya.spendwise.repository.specification;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecification {
    private ExpenseSpecification() {
    }

    public static Specification<Expense> belongsToUser(User user) {
        return ExpenseSpecifications.belongsToUser(user);
    }

    public static Specification<Expense> hasCategory(Category category) {
        return ExpenseSpecifications.hasCategory(category);
    }

    public static Specification<Expense> hasNote(String note) {
        return ExpenseSpecifications.hasNote(note);
    }
}