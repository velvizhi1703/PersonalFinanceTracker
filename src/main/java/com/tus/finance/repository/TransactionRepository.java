package com.tus.finance.repository;
import com.tus.finance.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByUserId(Long userId);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'CREDIT'")
	Double getTotalIncomeForUser(@Param("userId") Long userId);

	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'DEBIT'")
	Double getTotalExpenseForUser(@Param("userId") Long userId);

	@Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
	int countByUserId(@Param("userId") Long userId);

	
	List<Transaction> findAll();

}