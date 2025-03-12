package com.tus.finance.dto;

import java.util.List;

public class UserAllDTO {

    private Long id;
    private String name;
    private String email;
    private double totalExpenses;
    private double totalIncome;
    private long transactionCount;
    private String status;
    
    public UserAllDTO() {
    	
    }

    // Constructor that matches the query result
    public UserAllDTO(Long id, String name, String email, double totalExpenses, double totalIncome, long transactionCount, String status) {
        this.id = (id != null) ? id : 0L;
        this.name = (name != null) ? name : "Unknown";
        this.email = (email != null) ? email : "Unknown";
        this.totalExpenses = (totalExpenses != 0) ? totalExpenses : 0.0;
        this.totalIncome = (totalIncome != 0) ? totalIncome : 0.0;
        this.transactionCount = (transactionCount != 0) ? transactionCount : 0L;
        this.status = (status != null) ? status : "Unknown";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(long transactionCount) {
        this.transactionCount = transactionCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
