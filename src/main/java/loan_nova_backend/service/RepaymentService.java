package loan_nova_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import loan_nova_backend.entity.Loan;
import loan_nova_backend.entity.Repayment;
import loan_nova_backend.repository.LoanRepository;
import loan_nova_backend.repository.RepaymentRepository;

@Service
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    private final LoanRepository loanRepository;

    public RepaymentService(
            RepaymentRepository repaymentRepository,
            LoanRepository loanRepository) {

        this.repaymentRepository = repaymentRepository;
        this.loanRepository = loanRepository;
    }

    // GENERATE EMI SCHEDULE
    public List<Repayment> generateSchedule(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        if (loan.getEmi() == null ||
                loan.getTenure() == null) {

            throw new RuntimeException(
                    "Loan EMI or tenure is missing"
            );
        }

        if (repaymentRepository.existsByLoanId(loanId)) {

            throw new RuntimeException(
                    "Repayment schedule already exists"
            );
        }

        for (int i = 1; i <= loan.getTenure(); i++) {

            Repayment repayment = new Repayment();

            repayment.setInstallmentNumber(i);

            repayment.setAmount(loan.getEmi());

            repayment.setDueDate(
                    LocalDate.now().plusMonths(i)
            );

            repayment.setStatus("PENDING");

            repayment.setLoan(loan);

            repaymentRepository.save(repayment);
        }

        return repaymentRepository.findByLoanId(loanId);
    }

    // GET LOAN REPAYMENTS
    public List<Repayment> getLoanRepayments(Long loanId) {

        loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        return repaymentRepository.findByLoanId(loanId);
    }

    // GET USER REPAYMENTS
    public List<Repayment> getUserRepayments(Long userId) {

        return repaymentRepository
                .findByLoanUserId(userId);
    }

    // GET REPAYMENT BY ID
    public Repayment getRepaymentById(Long id) {

        return repaymentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Repayment not found"
                        ));
    }

    // MARK EMI AS PAID
    public Repayment markAsPaid(
            Long id,
            String paymentReference) {

        Repayment repayment =
                getRepaymentById(id);

        if ("PAID".equalsIgnoreCase(
                repayment.getStatus())) {

            throw new RuntimeException(
                    "This EMI is already paid"
            );
        }

        repayment.setStatus("PAID");

        repayment.setPaidDate(
                LocalDate.now()
        );

        repayment.setPaymentReference(
                paymentReference
        );

        return repaymentRepository.save(
                repayment
        );
    }

    // SUMMARY
    public Object getSummary(Long loanId) {

        List<Repayment> repayments =
                getLoanRepayments(loanId);

        long totalInstallments =
                repayments.size();

        long paidInstallments =
                repayments.stream()
                        .filter(r ->
                                "PAID".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .count();

        long pendingInstallments =
                totalInstallments -
                        paidInstallments;

        double totalAmount =
                repayments.stream()
                        .mapToDouble(
                                Repayment::getAmount
                        )
                        .sum();

        double paidAmount =
                repayments.stream()
                        .filter(r ->
                                "PAID".equalsIgnoreCase(
                                        r.getStatus()
                                ))
                        .mapToDouble(
                                Repayment::getAmount
                        )
                        .sum();

        double remainingAmount =
                totalAmount - paidAmount;

        return java.util.Map.of(
                "loanId", loanId,
                "totalInstallments",
                totalInstallments,
                "paidInstallments",
                paidInstallments,
                "pendingInstallments",
                pendingInstallments,
                "totalAmount",
                Math.round(totalAmount * 100.0) / 100.0,
                "paidAmount",
                Math.round(paidAmount * 100.0) / 100.0,
                "remainingAmount",
                Math.round(remainingAmount * 100.0) / 100.0
        );
    }
}