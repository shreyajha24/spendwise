package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {
    List<Expense> findByCategory(String category);

    List<Expense> findByUser_Id(Long userId);

    List<Expense> findByUser_IdAndCategory(Long userId, String category);

    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_Id(Long userId);
}
