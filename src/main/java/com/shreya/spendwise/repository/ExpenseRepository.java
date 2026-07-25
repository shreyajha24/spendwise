package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Category;
import com.shreya.spendwise.entity.Expense;
import com.shreya.spendwise.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository
        extends JpaRepository<Expense, Long> {
    List<Expense> findByCategory(Category category);

    List<Expense> findByUser_Id(Long userId);

    List<Expense> findByUser(User user);

    List<Expense> findByUser_IdAndCategory(Long userId, Category category);

    Page<Expense> findByUser(User user, Pageable pageable);

    Page<Expense> findByUserAndCategory(User user, Category category, Pageable pageable);

    Page<Expense> findByUserAndNoteContainingIgnoreCase(User user, String note, Pageable pageable);

    Page<Expense> findByUserAndCategoryAndNoteContainingIgnoreCase(
            User user, Category category, String note, Pageable pageable);

    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_Id(Long userId);
}
