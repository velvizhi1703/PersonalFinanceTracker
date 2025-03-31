package com.tus.finance.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TransactionDTOTest {

    @Test
    public void testGetterAndSetter() {
        // Create a new DTO
        TransactionDTO dto = new TransactionDTO();

        // Test setters and getters
        dto.setId(1L);
        assertEquals(1L, dto.getId());

        dto.setDescription("Test Transaction");
        assertEquals("Test Transaction", dto.getDescription());

        dto.setAmount(100.50);
        assertEquals(100.50, dto.getAmount(), 0.001); // Delta for double comparison

        dto.setType("INCOME");
        assertEquals("INCOME", dto.getType());
    }

    @Test
    public void testEqualsAndHashCode() {
        TransactionDTO dto1 = new TransactionDTO();
        dto1.setId(1L);
        dto1.setDescription("Test");
        dto1.setAmount(100.0);
        dto1.setType("EXPENSE");

        TransactionDTO dto2 = new TransactionDTO();
        dto2.setId(1L);
        dto2.setDescription("Test");
        dto2.setAmount(100.0);
        dto2.setType("EXPENSE");

        // Test equals()
        assertEquals(dto1, dto2);

        // Test hashCode()
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testToString() {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(1L);
        dto.setDescription("Test");
        dto.setAmount(100.0);
        dto.setType("EXPENSE");

        String toStringOutput = dto.toString();
        assertTrue(toStringOutput.contains("TransactionDTO"));
        assertTrue(toStringOutput.contains("id=1"));
        assertTrue(toStringOutput.contains("description=Test"));
        assertTrue(toStringOutput.contains("amount=100.0"));
        assertTrue(toStringOutput.contains("type=EXPENSE"));
    }
}