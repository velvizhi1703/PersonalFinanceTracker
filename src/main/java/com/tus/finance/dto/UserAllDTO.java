package com.tus.finance.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;


@Relation(collectionRelation = "users")
public class UserAllDTO extends RepresentationModel<UserAllDTO> {
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
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Double getTotalExpense() { return totalExpense; }
    public Double getTotalIncome() { return totalIncome; }
    public Integer getNumTransactions() { return numTransactions; }
    public String getStatus() { return status; }
}
