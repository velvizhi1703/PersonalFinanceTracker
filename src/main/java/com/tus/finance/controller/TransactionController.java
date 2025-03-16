package com.tus.finance.controller;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.model.TransactionType;
import com.tus.finance.repository.BudgetRepository;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.security.JwtUtil;
import com.tus.finance.service.TransactionService;
import com.tus.finance.service.UserService;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	  private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

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
    public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getUserTransactions(Authentication authentication) {
        String userEmail = authentication.getName();
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isPresent()) {
            List<Transaction> transactions = transactionService.getTransactionsByUserId(user.get().getId());

            List<EntityModel<Transaction>> transactionResources = transactions.stream()
            	    .map(transaction -> {
            	        EntityModel<Transaction> entityModel = EntityModel.of(transaction,
            	            linkTo(methodOn(TransactionController.class)
            	                .getTransactionById(transaction.getId(), authentication)).withSelfRel(),
            	            linkTo(methodOn(TransactionController.class)
            	                .getUserTransactions(authentication)).withRel("user-transactions")
            	        );
            	        return entityModel;
            	    })
            	    .collect(Collectors.toList());


            return ResponseEntity.ok(CollectionModel.of(transactionResources,
                linkTo(methodOn(TransactionController.class).getUserTransactions(authentication)).withSelfRel()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    /**
     * ✅ Admin fetches transactions for a specific user ID
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getTransactionsByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<EntityModel<Transaction>> transactionResources = transactions.stream()
            .map(transaction -> EntityModel.of(transaction,
                linkTo(methodOn(TransactionController.class).getTransactionsByUserId(userId)).withSelfRel(),
                linkTo(methodOn(TransactionController.class).getAllTransactions()).withRel("all-transactions")))
            .collect(Collectors.toList());

        CollectionModel<EntityModel<Transaction>> collectionModel = CollectionModel.of(transactionResources,
                linkTo(methodOn(TransactionController.class).getTransactionsByUserId(userId)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * ✅ Admin fetches ALL transactions for ALL users
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();

        List<EntityModel<Transaction>> transactionResources = transactions.stream()
                .map(transaction -> EntityModel.of(transaction,
                        linkTo(methodOn(TransactionController.class).getTransactionById(transaction.getId(), null)).withSelfRel(),
                        linkTo(methodOn(TransactionController.class).getAllTransactions()).withRel("all-transactions")
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(CollectionModel.of(transactionResources,
                linkTo(methodOn(TransactionController.class).getAllTransactions()).withSelfRel()
        ));
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
    public ResponseEntity<EntityModel<Transaction>> addTransaction(@RequestBody Transaction transaction, Authentication authentication) {
        if (transaction == null) {
            return ResponseEntity.badRequest().body(null);
        }

        String userEmail = authentication.getName();
        Optional<User> user = userService.findByEmail(userEmail);

        if (user.isPresent()) {
            transaction.setUser(user.get());
            Transaction savedTransaction = transactionService.addTransaction(transaction);

            return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(savedTransaction,
                    linkTo(methodOn(TransactionController.class).getTransactionById(savedTransaction.getId(), authentication)).withSelfRel(),
                    linkTo(methodOn(TransactionController.class).getTransactionsByUserId(user.get().getId())).withRel("user-transactions")
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    /**
     * ✅ Delete a transaction (Only Admin can delete transactions)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);

        if (transactionOpt.isPresent()) {
            transactionService.deleteTransaction(id);
            return ResponseEntity.ok(EntityModel.of(transactionOpt.get(),
                    linkTo(methodOn(TransactionController.class).getAllTransactions()).withRel("all-transactions")
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Transaction not found.");
        }
    }


    /**
     * ✅ Admin sees ALL transactions, while Users see only their own
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EntityModel<Map<String, Object>>> getDashboardStats(@RequestHeader("Authorization") String token) {
        String userEmail = jwtUtil.extractUsername(token.substring(7)); // ✅ Extract email
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Fetch user-specific transactions
        double income = Optional.ofNullable(transactionRepository.getTotalIncomeForUser(user.getId())).orElse(0.0);
        double expense = Optional.ofNullable(transactionRepository.getTotalExpenseForUser(user.getId())).orElse(0.0);
        double cashInHand = income - expense;
        int numTransactions = transactionRepository.countByUserId(user.getId());

        double budgetPercentage = 0.75;  // Example: 75% of income
        double budgetAmount = income * budgetPercentage;
        double remainingBudget = budgetAmount - expense; 
        double overBudget = Math.max(0, expense - budgetAmount);

        // ✅ Compute Expense Breakdown by Category
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        Map<String, Double> expenseBreakdown = transactions.stream()
            .filter(t -> "DEBIT".equalsIgnoreCase(t.getType()))
            .collect(Collectors.groupingBy(
                Transaction::getCategory, 
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        // ✅ Prepare Budget Details
        Map<String, Double> budgetDetails = new HashMap<>();
        budgetDetails.put("total_budget", budgetAmount);
        budgetDetails.put("spent", expense);
        budgetDetails.put("remaining", remainingBudget);
        budgetDetails.put("overBudget", overBudget); 

        // ✅ Prepare API Response (With Charts Data)
        Map<String, Object> dashboardData = Map.of(
            "income", income,
            "expense", expense,
            "cash_in_hand", cashInHand,
            "num_transactions", numTransactions,
            "expenseBreakdown", expenseBreakdown,
            "budget", budgetDetails
        );

        // ✅ Add HATEOAS Links
        EntityModel<Map<String, Object>> entityModel = EntityModel.of(dashboardData,
            linkTo(methodOn(TransactionController.class).getDashboardStats(token)).withSelfRel(),
            linkTo(methodOn(TransactionController.class).getUserTransactions(null)).withRel("user-transactions"),
            linkTo(methodOn(TransactionController.class).getTransactionsByUserId(user.getId())).withRel("user-all-transactions")
        );

        return ResponseEntity.ok(entityModel);
    }
}