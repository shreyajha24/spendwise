package com.shreya.spendwise.dto;

public class MonthlySpendingResponse {
    private Integer year;
    private Integer month;
    private Double totalAmount;

    public MonthlySpendingResponse(Integer year, Integer month, Double totalAmount) {
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
