package com.tus.finance.dto;

import lombok.Data;

@Data
public class TransactionDTO {
    private Long id;
    private String description;
    private double amount;
    private String type;
}
