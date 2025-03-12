package com.tus.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore  // ✅ Prevents password from appearing in JSON response
    @Column(nullable = false)
    private String password;
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
    @Column(nullable = false)
    private boolean enabled;
 // ✅ Ensures roles are never null
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    @Column(name = "total_income", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
    private double totalIncome;

    @Column(name = "total_expense", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
    private double totalExpense;

    @Column(name = "cash_in_hand", nullable = false, columnDefinition = "DOUBLE DEFAULT 0")
    private double cashInHand;

    @Column(name = "num_transactions", nullable = false, columnDefinition = "INT DEFAULT 0")
    private int numTransactions;

    // ✅ Getters and Setters
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public boolean enabled() {
        return enabled();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public double getCashInHand() { return cashInHand; }
    public void setCashInHand(double cashInHand) { this.cashInHand = cashInHand; }

    public int getNumTransactions() { return numTransactions; }
    public void setNumTransactions(int numTransactions) { this.numTransactions = numTransactions; }
}
