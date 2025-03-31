package com.tus.finance.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tus.finance.dto.TransactionDTO;

class TransactionDTOTest {

    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        transactionDTO = new TransactionDTO();
    }

    @Test
    void testIdGetterAndSetter() {
        Long expectedId = 1L;
        transactionDTO.setId(expectedId);
        assertEquals(expectedId, transactionDTO.getId());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        String expectedDescription = "Test Transaction";
        transactionDTO.setDescription(expectedDescription);
        assertEquals(expectedDescription, transactionDTO.getDescription());
    }

    @Test
    void testAmountGetterAndSetter() {
        double expectedAmount = 100.50;
        transactionDTO.setAmount(expectedAmount);
        assertEquals(expectedAmount, transactionDTO.getAmount(), 0.001);
    }

    @Test
    void testTypeGetterAndSetter() {
        String expectedType = "CREDIT";
        transactionDTO.setType(expectedType);
        assertEquals(expectedType, transactionDTO.getType());
    }

    @Test
    void testAllArgsConstructor() {
        Long expectedId = 1L;
        String expectedDescription = "Test Transaction";
        double expectedAmount = 100.50;
        String expectedType = "CREDIT";

        TransactionDTO dto = new TransactionDTO();
        dto.setId(expectedId);
        dto.setDescription(expectedDescription);
        dto.setAmount(expectedAmount);
        dto.setType(expectedType);

        assertEquals(expectedId, dto.getId());
        assertEquals(expectedDescription, dto.getDescription());
        assertEquals(expectedAmount, dto.getAmount(), 0.001);
        assertEquals(expectedType, dto.getType());
    }
}