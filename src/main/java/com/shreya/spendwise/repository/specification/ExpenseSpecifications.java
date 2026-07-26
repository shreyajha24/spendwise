package com.shreya.spendwise.repository.specification;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> byFilter(User user, Category category, String note) {
        Specification<Expense> specification = Specification
                .where(belongsToUser(user))
                .and(category != null ? hasCategory(category) : null)
                .and(note != null && !note.isBlank() ? hasNote(note.trim()) : null);
        return specification;
    }

    public static Specification<Expense> belongsToUser(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<Expense> hasCategory(Category category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Expense> hasNote(String note) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), "%" + note.toLowerCase() + "%");
    }
}
