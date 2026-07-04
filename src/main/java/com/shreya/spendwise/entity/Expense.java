package com.shreya.spendwise.entity;
import jakarta.persistence.*;
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String category;
    private double amount;
    public Expense() {
    }
    public Expense(String category, double amount) {
        this.category =category;
        this.amount = amount;
    }
    public Long getId() {
        return id;
    }
    public String getCategory() {
        return category;
    }
    public double getAmount() {
        return amount;
    }
}
