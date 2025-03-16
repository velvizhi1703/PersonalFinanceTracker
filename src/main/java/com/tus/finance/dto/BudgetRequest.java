package com.tus.finance.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {
    private Long userId;
    private int month;
    private int year;
    private double amount;
}
