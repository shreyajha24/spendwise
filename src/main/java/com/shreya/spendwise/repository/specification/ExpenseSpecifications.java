package com.shreya.spendwise.repository.specification;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecifications {
    private ExpenseSpecifications() {
    }

    public static Specification<Expense> byFilter(User user, Category category, String note) {
        Specification<Expense> specification = belongsToUser(user);
        if (category != null) {
            specification = specification.and(hasCategory(category));
        }
        if (note != null && !note.isBlank()) {
            specification = specification.and(noteContains(note.trim()));
        }
        return specification;
    }

    public static Specification<Expense> belongsToUser(User user) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"), user);
    }

    public static Specification<Expense> hasCategory(Category category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Expense> noteContains(String note) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), "%" + note.toLowerCase() + "%");
    }
}
