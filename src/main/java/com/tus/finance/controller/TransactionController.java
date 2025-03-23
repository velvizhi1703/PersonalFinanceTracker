package com.tus.finance.controller;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import com.tus.finance.security.JwtUtil;
import com.tus.finance.service.TransactionService;
import com.tus.finance.service.UserService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
	
	private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
	private static final String ALL_TRANSACTIONS_REL = "all-transactions";
	private final TransactionService transactionService;
	private final UserService userService;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final JwtUtil jwtUtil;


	public TransactionController(TransactionService transactionService, UserService userService,UserRepository userRepository,TransactionRepository transactionRepository,JwtUtil jwtUtil) { 
		this.transactionService = transactionService;
		this.userService = userService;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.jwtUtil=jwtUtil;
		
	}

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
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getTransactionsByUserId(@PathVariable Long userId) {
		List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);

		if (transactions.isEmpty()) {
			 logger.warn("Transactions not found for user ID: {}", userId);
			return ResponseEntity.notFound().build();
		}
         
		List<EntityModel<Transaction>> transactionResources = transactions.stream()
				.map(transaction -> EntityModel.of(transaction,
						linkTo(methodOn(TransactionController.class).getTransactionsByUserId(userId)).withSelfRel(),
						linkTo(methodOn(TransactionController.class).getAllTransactions()).withRel(ALL_TRANSACTIONS_REL)))
				.collect(Collectors.toList());

		CollectionModel<EntityModel<Transaction>> collectionModel = CollectionModel.of(transactionResources,
				linkTo(methodOn(TransactionController.class).getTransactionsByUserId(userId)).withSelfRel());

		return ResponseEntity.ok(collectionModel);
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CollectionModel<EntityModel<Transaction>>> getAllTransactions() {
		List<Transaction> transactions = transactionService.getAllTransactions();

		List<EntityModel<Transaction>> transactionResources = transactions.stream()
				.map(transaction -> EntityModel.of(transaction,
						linkTo(methodOn(TransactionController.class).getTransactionById(transaction.getId(), null)).withSelfRel(),
						linkTo(methodOn(TransactionController.class).getAllTransactions()).withRel(ALL_TRANSACTIONS_REL)
		                ))
				.collect(Collectors.toList());

		return ResponseEntity.ok(CollectionModel.of(transactionResources,
				linkTo(methodOn(TransactionController.class).getAllTransactions()).withSelfRel()
				));
	}

	@GetMapping("/{transactionId}")
	@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Transaction> getTransactionById(@PathVariable Long transactionId, Authentication authentication) {
		Optional<Transaction> transaction = transactionService.getTransactionById(transactionId);

		if (transaction.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Optional<User> user = userService.findByEmail(authentication.getName());
		if (user.isPresent() && !user.get().getRoles().contains("ROLE_ADMIN")) {
			if (!transaction.get().getId().equals(user.get().getId())) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
			}
		}

		return ResponseEntity.ok(transaction.get());
	}
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

		@DeleteMapping("/{id}")
		@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
		public ResponseEntity<Object> deleteTransaction(@PathVariable Long id) {
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

		@GetMapping("/dashboard")
		@PreAuthorize("hasRole('USER')")
		public ResponseEntity<EntityModel<Map<String, Object>>> getDashboardStats(@RequestHeader("Authorization") String token) {
			String userEmail = jwtUtil.extractUsername(token.substring(7)); 
			User user = userRepository.findByEmail(userEmail)
					.orElseThrow(() -> new RuntimeException("User not found"));

			double income = Optional.ofNullable(transactionRepository.getTotalIncomeForUser(user.getId())).orElse(0.0);
			double expense = Optional.ofNullable(transactionRepository.getTotalExpenseForUser(user.getId())).orElse(0.0);
			double cashInHand = income - expense;
			int numTransactions = transactionRepository.countByUserId(user.getId());

			double budgetPercentage = 0.75;  
			double budgetAmount = income * budgetPercentage;
			double remainingBudget = budgetAmount - expense; 
			double overBudget = Math.max(0, expense - budgetAmount);

			List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
			Map<String, Double> expenseBreakdown = transactions.stream()
					.filter(t -> "DEBIT".equalsIgnoreCase(t.getType()))
					.collect(Collectors.groupingBy(
							Transaction::getCategory, 
							Collectors.summingDouble(t -> t.getAmount().doubleValue())
							));

			Map<String, Double> budgetDetails = new HashMap<>();
			budgetDetails.put("total_budget", budgetAmount);
			budgetDetails.put("spent", expense);
			budgetDetails.put("remaining", remainingBudget);
			budgetDetails.put("overBudget", overBudget); 

			Map<String, Object> dashboardData = Map.of(
					"income", income,
					"expense", expense,
					"cash_in_hand", cashInHand,
					"num_transactions", numTransactions,
					"expenseBreakdown", expenseBreakdown,
					"budget", budgetDetails
					);

			EntityModel<Map<String, Object>> entityModel = EntityModel.of(dashboardData,
					linkTo(methodOn(TransactionController.class).getDashboardStats(token)).withSelfRel(),
					linkTo(methodOn(TransactionController.class).getUserTransactions(null)).withRel("user-transactions"),
					linkTo(methodOn(TransactionController.class).getTransactionsByUserId(user.getId())).withRel("user-all-transactions")
					);

			return ResponseEntity.ok(entityModel);
		}
	}