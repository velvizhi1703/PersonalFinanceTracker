package com.tus.finance.controller;

import com.tus.finance.model.Budget;
import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.BudgetRepository;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.security.JwtUtil;
import com.tus.finance.service.TransactionService;
import com.tus.finance.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final JwtUtil jwtUtil;

    private final BudgetRepository budgetRepository;
    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService,UserRepository userRepository,TransactionRepository transactionRepository,BudgetRepository budgetRepository, JwtUtil jwtUtil) { 
        this.transactionService = transactionService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.jwtUtil=jwtUtil;
        this.budgetRepository = budgetRepository;
    }

    /**
     * ✅ User fetches ONLY their own transactions
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getUserTransactions(Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<User> user = userService.findByEmail(userEmail); // ✅ Now userService is available

        if (user.isPresent()) {
            List<Transaction> transactions = transactionService.getTransactionsByUserId(user.get().getId());
            return ResponseEntity.ok(transactions);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * ✅ Admin fetches transactions for a specific user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getTransactionsByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * ✅ Admin fetches ALL transactions for ALL users
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions(); // Call service layer
        return ResponseEntity.ok(transactions);
    }


    /**
     * ✅ Fetch a specific transaction by its ID (Both admin and user allowed)
     */
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long transactionId, Authentication authentication) {
        Optional<Transaction> transaction = transactionService.getTransactionById(transactionId);

        if (transaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Allow admins to fetch any transaction, but restrict users to their own
        Optional<User> user = userService.findByEmail(authentication.getName());
        if (user.isPresent() && !user.get().getRoles().contains("ROLE_ADMIN")) {
            if (!transaction.get().getId().equals(user.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        }

        return ResponseEntity.ok(transaction.get());
    }
    /**
     * ✅ Add a new transaction (User or Admin can add transactions)
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        if (transaction == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Transaction savedTransaction = transactionService.addTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTransaction);
    }

    /**
     * ✅ Delete a transaction (Only Admin can delete transactions)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✅ Admin sees ALL transactions, while Users see only their own
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')") // ✅ Restrict to USER role
    public ResponseEntity<?> getDashboardStats(@RequestHeader("Authorization") String token) {
        String userEmail = jwtUtil.extractUsername(token.substring(7)); // ✅ Extract email
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Fetch user-specific transactions
        double income = Optional.ofNullable(transactionRepository.getTotalIncomeForUser(user.getId())).orElse(0.0);
        double expense = Optional.ofNullable(transactionRepository.getTotalExpenseForUser(user.getId())).orElse(0.0);
        double cashInHand = income - expense;
        int numTransactions = transactionRepository.countByUserId(user.getId());
        
        double budgetAmount = income * 0.733; // 73.33% of income
        double remainingBudget = Math.max(0, budgetAmount - expense);

        // ✅ Compute Expense Breakdown by Category
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        Map<String, Double> expenseBreakdown = transactions.stream()
            .filter(t -> "DEBIT".equalsIgnoreCase(t.getType())) // Only Debit Transactions
            .collect(Collectors.groupingBy(
                Transaction::getCategory, 
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        Map<String, Double> budgetDetails = new HashMap<>();
        budgetDetails.put("total_budget", budgetAmount);
        budgetDetails.put("spent", expense);
        budgetDetails.put("remaining", remainingBudget);

        // ✅ Update user data (No change)
        user.setTotalIncome(income);
        user.setTotalExpense(expense);
        user.setCashInHand(cashInHand);
        user.setNumTransactions(numTransactions);
        userRepository.save(user); // Save changes

        // ✅ Prepare API Response (With Charts Data)
        Map<String, Object> dashboardData = Map.of(
            "income", income,
            "expense", expense,
            "cash_in_hand", cashInHand,
            "num_transactions", numTransactions,
            "expenseBreakdown", expenseBreakdown,  // ✅ Required for Pie Chart
            "budget", budgetDetails                 // ✅ Required for Budget Meter
        );

        return ResponseEntity.ok(dashboardData);
    }

}
