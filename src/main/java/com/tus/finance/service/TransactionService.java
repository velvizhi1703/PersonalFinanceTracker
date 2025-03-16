package com.tus.finance.service;

import com.tus.finance.model.Role;
import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        
        // ✅ If only one transaction exists, ensure it's still a list
        return transactions != null ? transactions : new ArrayList<>();
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
//    public double getCashInHand(Long userId) {
//        return getTotalIncomeForUser(userId) - getTotalExpenseForUser(userId);
//    }


    public long getTransactionCount(Long userId) {
        return transactionRepository.countByUserId(userId);
    }

//    public List<Transaction> getUserTransactionsByMonth(Long userId, int month, int year) {
//        return transactionRepository.findByUserIdAndMonthAndYear(userId, month, year);
//    }
//    public double getTotalIncomeForUser(Long userId) {
//        Double income = transactionRepository.getTotalIncomeForUser(userId);
//        return (income != null) ? income : 0.0;  // ✅ Ensure 0.0 if `null`
//    }
//
//    public double getTotalExpenseForUser(Long userId) {
//        Double expense = transactionRepository.getTotalExpenseForUser(userId);
//        return (expense != null) ? expense : 0.0;  // ✅ Ensure 0.0 if `null`
//    }
//
//    public List<Object[]> getUserSummary() {
//        return transactionRepository.getAggregatedDataForAllUsers(Role.ROLE_ADMIN);
//    }

}
