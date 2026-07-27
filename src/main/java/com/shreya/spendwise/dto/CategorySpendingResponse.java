package com.shreya.spendwise.dto;

import com.shreya.spendwise.entity.Category;

public class CategorySpendingResponse {
    private Category category;
    private Double totalAmount;

    public CategorySpendingResponse(Category category, Double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
