package com.tus.finance.repository;

import com.tus.finance.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
	
	@Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.month = :month AND b.year = :year")
	Optional<Budget> findByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);
	@Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.month = MONTH(CURRENT_DATE) AND b.year = YEAR(CURRENT_DATE)")
	Optional<Budget> findCurrentMonthBudget(@Param("userId") Long userId);
}

