package com.shreya.spendwise.repository;

import com.shreya.spendwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    Optional<Expense> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_Id(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    Double findTotalSpendingBetweenDates(@Param("userId") Long userId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(MAX(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    Double findHighestExpense(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    Double findAverageExpense(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(MIN(e.amount), 0) FROM Expense e WHERE e.user.id = :userId")
    Double findLowestExpense(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    Long countExpensesBetweenDates(@Param("userId") Long userId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT new com.shreya.spendwise.dto.MonthlySpendingResponse(YEAR(e.date), MONTH(e.date), SUM(e.amount)) " +
            "FROM Expense e WHERE e.user.id = :userId GROUP BY YEAR(e.date), MONTH(e.date) ORDER BY YEAR(e.date), MONTH(e.date)")
    List<com.shreya.spendwise.dto.MonthlySpendingResponse> findMonthlySpending(@Param("userId") Long userId);

    @Query("SELECT new com.shreya.spendwise.dto.CategorySpendingResponse(e.category, SUM(e.amount)) " +
            "FROM Expense e WHERE e.user.id = :userId GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<com.shreya.spendwise.dto.CategorySpendingResponse> findCategoryWiseSpending(@Param("userId") Long userId);
}
