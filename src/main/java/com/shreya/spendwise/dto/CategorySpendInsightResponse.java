package com.shreya.spendwise.dto;

import com.shreya.spendwise.entity.Category;

public class CategorySpendInsightResponse {
    private Category category;
    private Double amount;
    private Double percentage;

    public CategorySpendInsightResponse() {
    }

    public CategorySpendInsightResponse(Category category, Double amount, Double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
