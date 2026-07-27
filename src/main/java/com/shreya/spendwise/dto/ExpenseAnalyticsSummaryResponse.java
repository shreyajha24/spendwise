package com.shreya.spendwise.dto;

public class ExpenseAnalyticsSummaryResponse {
    private Double totalSpendingThisMonth;
    private Double highestExpense;
    private Double averageExpense;
    private Double lowestExpense;
    private Long numberOfExpenses;
    private Long numberOfExpensesThisMonth;

    public ExpenseAnalyticsSummaryResponse(Double totalSpendingThisMonth, Double highestExpense, Double averageExpense,
                                           Double lowestExpense, Long numberOfExpenses, Long numberOfExpensesThisMonth) {
        this.totalSpendingThisMonth = totalSpendingThisMonth;
        this.highestExpense = highestExpense;
        this.averageExpense = averageExpense;
        this.lowestExpense = lowestExpense;
        this.numberOfExpenses = numberOfExpenses;
        this.numberOfExpensesThisMonth = numberOfExpensesThisMonth;
    }

    public Double getTotalSpendingThisMonth() {
        return totalSpendingThisMonth;
    }

    public void setTotalSpendingThisMonth(Double totalSpendingThisMonth) {
        this.totalSpendingThisMonth = totalSpendingThisMonth;
    }

    public Double getHighestExpense() {
        return highestExpense;
    }

    public void setHighestExpense(Double highestExpense) {
        this.highestExpense = highestExpense;
    }

    public Double getAverageExpense() {
        return averageExpense;
    }

    public void setAverageExpense(Double averageExpense) {
        this.averageExpense = averageExpense;
    }

    public Double getLowestExpense() {
        return lowestExpense;
    }

    public void setLowestExpense(Double lowestExpense) {
        this.lowestExpense = lowestExpense;
    }

    public Long getNumberOfExpenses() {
        return numberOfExpenses;
    }

    public void setNumberOfExpenses(Long numberOfExpenses) {
        this.numberOfExpenses = numberOfExpenses;
    }

    public Long getNumberOfExpensesThisMonth() {
        return numberOfExpensesThisMonth;
    }

    public void setNumberOfExpensesThisMonth(Long numberOfExpensesThisMonth) {
        this.numberOfExpensesThisMonth = numberOfExpensesThisMonth;
    }
}
