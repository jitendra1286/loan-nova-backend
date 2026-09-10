package loan_nova_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import loan_nova_backend.entity.LoanApplication;
import loan_nova_backend.entity.User;

public interface LoanApplicationRepository
        extends JpaRepository<LoanApplication, Long> {

    List<LoanApplication> findByUser(User user);

    List<LoanApplication> findByStatus(String status);

    List<LoanApplication> findByLoanId(Long loanId);
}