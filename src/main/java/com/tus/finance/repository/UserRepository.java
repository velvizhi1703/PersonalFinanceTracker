package com.tus.finance.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import com.tus.finance.model.Role;
import com.tus.finance.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends CrudRepository<User, Long> {

	Optional<User> findByEmail(String email);

	@Query("SELECT r FROM User u JOIN u.roles r WHERE u.id = :userId")
	Set<Role> findRolesByUserId(Long userId);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE'")
	double getTotalExpensesByUserId(@Param("userId") Long userId);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME'")
	double getTotalIncomeByUserId(@Param("userId") Long userId);

	@Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
	long getTransactionCountByUserId(@Param("userId") Long userId);

	@Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'CREDIT'")
	Double getTotalIncomeForAllUsers();

	@Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM Transaction t WHERE t.type = 'DEBIT'")
	Double getTotalExpenseForAllUsers();

	@Query("SELECT COUNT(t) FROM Transaction t")
	int countAllTransactions();


	@Query("SELECT u.id, u.name, u.email, " +
			"(SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = u.id AND t.type = 'EXPENSE'), " +
			"(SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = u.id AND t.type = 'INCOME'), " +
			"(SELECT COALESCE(COUNT(t), 0) FROM Transaction t WHERE t.user.id = u.id), " +
			"CASE WHEN u.enabled = true THEN 'Enabled' ELSE 'Disabled' END " +
			"FROM User u")
	List<Object[]> getRawUserData();

}

