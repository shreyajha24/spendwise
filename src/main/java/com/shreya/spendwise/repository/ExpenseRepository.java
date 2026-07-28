package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_Id(Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double findTotalSpendingByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Long countTotalExpensesByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double findHighestExpenseByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Double findAverageExpenseByUserId(@Param("userId") Long userId);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> findCategoryWiseSpendingByUserId(@Param("userId") Long userId);

    @Query("SELECT YEAR(e.date), MONTH(e.date), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY YEAR(e.date) DESC, MONTH(e.date) DESC")
    List<Object[]> findMonthlySpendingByUserId(@Param("userId") Long userId);
}

