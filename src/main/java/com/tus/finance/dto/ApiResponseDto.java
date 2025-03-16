package com.tus.finance.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseDto<T> {
    private boolean success;
    private String message;
    private T data;
}

