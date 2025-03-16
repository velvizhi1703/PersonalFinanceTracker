package com.tus.finance.service;

import com.tus.finance.dto.ApiResponseDto;
import com.tus.finance.dto.BudgetRequest;
import com.tus.finance.model.Budget;
import com.tus.finance.model.User;
import com.tus.finance.repository.BudgetRepository;
import com.tus.finance.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {
	@Autowired
	private final BudgetRepository budgetRepository;
	private final UserRepository userRepository;
	public BudgetService(BudgetRepository budgetRepository,UserRepository userRepository) {
		this.budgetRepository = budgetRepository;
		this.userRepository = userRepository;
	}

	public ResponseEntity<ApiResponseDto<?>> createBudget(Long userId, BudgetRequest request) {
		Optional<User> userOpt = userRepository.findById(userId);

		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponseDto<>(false, "User not found", null));
		}

		User user = userOpt.get();

		Optional<Budget> existingBudget = budgetRepository.findByUserIdAndMonthAndYear(userId, request.getMonth(), request.getYear());

		if (existingBudget.isPresent()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponseDto<>(false, "Budget already exists", null));
		}

		Budget newBudget = new Budget();
		newBudget.setUser(user);
		newBudget.setAmount(request.getAmount());
		newBudget.setSpent(0);
		newBudget.setRemaining(request.getAmount());
		newBudget.setMonth(request.getMonth());
		newBudget.setYear(request.getYear());

		budgetRepository.save(newBudget);

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseDto<>(true, "Budget created successfully", newBudget));
	}

	public ResponseEntity<ApiResponseDto<?>> getBudgetByMonth(Long userId, int month, int year) {
		Optional<Budget> budget = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

		if (budget.isEmpty()) {
			return ResponseEntity.ok(new ApiResponseDto<>(false, "No budget found for this month & year", null));
		}

		return ResponseEntity.ok(new ApiResponseDto<>(true, "Budget retrieved successfully!", budget.get()));
	}

	public List<Budget> getAllBudgets() {
		return budgetRepository.findAll();
	}
	public Optional<Budget> getBudgetByMonthAndYear(Long userId, int month, int year) {
		return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);
	}

	public Optional<Budget> getCurrentMonthBudget(Long userId) {
		return budgetRepository.findCurrentMonthBudget(userId);
	}
}