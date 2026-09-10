package loan_nova_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import loan_nova_backend.entity.Loan;
import loan_nova_backend.entity.User;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUser(User user);
}