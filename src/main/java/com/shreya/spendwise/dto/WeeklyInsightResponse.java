package com.shreya.spendwise.dto;

import com.shreya.spendwise.entity.Category;

import java.time.LocalDate;
import java.util.List;

public class WeeklyInsightResponse {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private Double totalSpent;
    private Long totalTransactions;
    private Double averageExpense;
    private Category topCategory;
    private Double topCategorySpent;
    private List<CategorySpendInsightResponse> categoryBreakdown;
    private String summary;

    public WeeklyInsightResponse() {
    }

    public WeeklyInsightResponse(
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Double totalSpent,
            Long totalTransactions,
            Double averageExpense,
            Category topCategory,
            Double topCategorySpent,
            List<CategorySpendInsightResponse> categoryBreakdown,
            String summary) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.totalSpent = totalSpent;
        this.totalTransactions = totalTransactions;
        this.averageExpense = averageExpense;
        this.topCategory = topCategory;
        this.topCategorySpent = topCategorySpent;
        this.categoryBreakdown = categoryBreakdown;
        this.summary = summary;
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Double getAverageExpense() {
        return averageExpense;
    }

    public void setAverageExpense(Double averageExpense) {
        this.averageExpense = averageExpense;
    }

    public Category getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(Category topCategory) {
        this.topCategory = topCategory;
    }

    public Double getTopCategorySpent() {
        return topCategorySpent;
    }

    public void setTopCategorySpent(Double topCategorySpent) {
        this.topCategorySpent = topCategorySpent;
    }

    public List<CategorySpendInsightResponse> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(List<CategorySpendInsightResponse> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
