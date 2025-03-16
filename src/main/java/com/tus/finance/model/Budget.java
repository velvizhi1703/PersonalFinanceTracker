package com.tus.finance.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int month;  // Month of the budget (1-12)

    @Column(nullable = false)
    private int year;  // Year of the budget

    @Column(nullable = false)
    private double amount;  // Budgeted amount
    
    private double spent;  // ✅ Amount spent

    private double remaining;

    // ✅ Constructors
    public Budget() {}

    public Budget(User user, int month, int year, double amount) {
        this.user = user;
        this.month = month;
        this.year = year;
        this.amount = amount;
    }
    public double getSpent() {  // ✅ Getter for spent
        return spent;
    }

    public void setSpent(double spent) {  // ✅ Setter for spent
        this.spent = spent;
    }

    public double getRemaining() {  // ✅ Getter for remaining
        return remaining;
    }

    public void setRemaining(double remaining) {  // ✅ Setter for remaining
        this.remaining = remaining;
    }
    // ✅ Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double d) {
        this.amount = d;
    }
}
