package com.shreya.spendwise.dto;

import com.shreya.spendwise.entity.Category;

public class QuickExpenseTemplateResponse {
    private String templateKey;
    private String label;
    private Category category;
    private Double amount;
    private String defaultNote;

    public QuickExpenseTemplateResponse() {
    }

    public QuickExpenseTemplateResponse(String templateKey, String label, Category category, Double amount, String defaultNote) {
        this.templateKey = templateKey;
        this.label = label;
        this.category = category;
        this.amount = amount;
        this.defaultNote = defaultNote;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
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

    public String getDefaultNote() {
        return defaultNote;
    }

    public void setDefaultNote(String defaultNote) {
        this.defaultNote = defaultNote;
    }
}
