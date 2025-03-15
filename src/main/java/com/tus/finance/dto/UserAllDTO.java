package com.tus.finance.dto;

import java.util.List;



public class UserAllDTO {
    private Long id;
    private String name;
    private String email;
    private Double totalExpense;
    private Double totalIncome;
    private Integer numTransactions;
    private String status;

    public UserAllDTO(Long id, String name, String email, Double totalExpense, Double totalIncome, Integer numTransactions, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.totalExpense = totalExpense;
        this.totalIncome = totalIncome;
        this.numTransactions = numTransactions;
        this.status = status;
    }

    // ✅ Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Double getTotalExpense() { return totalExpense; }
    public Double getTotalIncome() { return totalIncome; }
    public Integer getNumTransactions() { return numTransactions; }
    public String getStatus() { return status; }
}
