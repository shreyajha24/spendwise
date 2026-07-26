package com.shreya.spendwise.repository.specification;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecification {
    private ExpenseSpecification() {
    }

    public static Specification<Expense> hasCategory(Category category) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("category"), category);
    }

    public static Specification<Expense> hasNote(String note) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), "%" + note.toLowerCase() + "%");
    }
}