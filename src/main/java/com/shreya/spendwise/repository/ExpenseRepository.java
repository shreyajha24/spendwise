package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {
}
