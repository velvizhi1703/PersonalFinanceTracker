package com.tus.finance.service;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository; // ✅ Initialize it in constructor
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }
    public List<Transaction> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserId(userId);
    }


    public Transaction addTransaction(Transaction transaction) {
        if (transaction.getUser() == null || transaction.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(transaction.getUser().getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        transaction.setUser(user);
        return transactionRepository.save(transaction);
    }


    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll(); // ✅ Ensure this returns data
    }
//    public BigDecimal getTotalIncome(Long userId) {
//        return transactionRepository.findAll().stream()
//                .filter(t -> t.getUser().getId().equals(userId) && "CREDIT".equalsIgnoreCase(t.getType()))
//                .map(Transaction::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//    public BigDecimal getTotalExpense(Long userId) {
//        return transactionRepository.findAll().stream()
//                .filter(t -> t.getUser().getId().equals(userId) && "DEBIT".equalsIgnoreCase(t.getType()))
//                .map(Transaction::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//
//    public BigDecimal getCashInHand(Long userId) {
//        return getTotalIncome(userId).subtract(getTotalExpense(userId));
//    }

    public long getTransactionCount(Long userId) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(userId))
                .count();
    }
//    public List<Transaction> getUserTransactionsByMonth(Long userId, int month, int year) {
//        return transactionRepository.findByUserIdAndMonthAndYear(userId, month, year);
//    }


}
