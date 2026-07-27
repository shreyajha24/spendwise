package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_Id(Long userId);
}
