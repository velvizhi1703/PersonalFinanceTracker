package com.tus.finance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal amount;
	//private String type; // CREDIT or DEBIT
	private LocalDate date;
	@Enumerated(EnumType.STRING)

	private TransactionType type;

	@Column(name = "category")
	private String category;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnore 
	private User user;

    public String getType() {
		return type.name(); 
	}

	@JsonProperty("userEmail") 
	public String getUserEmail() {
		return user.getEmail();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public BigDecimal getAmount() { return amount; }
	public void setAmount(BigDecimal amount) { this.amount = amount; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	//    public String getType() { return type; }
	//    public void setType(String type) { this.type = type; }

	public LocalDate getDate() { return date; }
	public void setDate(LocalDate date) { this.date = date; }

	public User getUser() { return user; }
	public void setUser(User user) { this.user = user; }
	public void setType(TransactionType type) { this.type = type; }
}
