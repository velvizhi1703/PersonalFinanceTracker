package com.tus.finance.service;

import com.tus.finance.model.Transaction;
import com.tus.finance.model.User;
import com.tus.finance.repository.TransactionRepository;
import com.tus.finance.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
	private TransactionRepository transactionRepository;
	private final UserRepository userRepository;

	public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository; 
	}

	public Optional<Transaction> getTransactionById(Long id) {
		return transactionRepository.findById(id);
	}
	public List<Transaction> getTransactionsByUserId(Long userId) {
		List<Transaction> transactions = transactionRepository.findByUserId(userId);

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
		return transactionRepository.findAll();
	}
	public long getTransactionCount(Long userId) {
		return transactionRepository.countByUserId(userId);
	}

}
