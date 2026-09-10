package loan_nova_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import loan_nova_backend.entity.Repayment;

public interface RepaymentRepository
        extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanId(Long loanId);

    List<Repayment> findByLoanUserId(Long userId);

    List<Repayment> findByStatus(String status);

    boolean existsByLoanId(Long loanId);
}