package com.shreya.spendwise.entity;
import jakarta.persistence.*;
@Entity
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private double amount;
    public Expense() {
    }
    public Expense(String title, double amount) {
        this.title = title;
        this.amount = amount;
    }
    public Long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public double getAmount() {
        return amount;
    }
}
