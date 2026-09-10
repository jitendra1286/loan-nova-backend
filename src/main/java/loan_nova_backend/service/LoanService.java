package loan_nova_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import loan_nova_backend.entity.Loan;
import loan_nova_backend.entity.User;
import loan_nova_backend.repository.LoanRepository;
import loan_nova_backend.repository.UserRepository;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public LoanService(
            LoanRepository loanRepository,
            UserRepository userRepository) {

        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    // CREATE LOAN
    public Loan createLoan(Loan loan, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        loan.setUser(user);

        if (loan.getStatus() == null ||
                loan.getStatus().isBlank()) {

            loan.setStatus("PENDING");
        }

        // EMI calculation
        if (loan.getAmount() != null &&
                loan.getInterestRate() != null &&
                loan.getTenure() != null) {

            double principal = loan.getAmount();
            double annualRate = loan.getInterestRate();
            int months = loan.getTenure();

            double monthlyRate =
                    annualRate / 12 / 100;

            double emi;

            // 0% interest
            if (monthlyRate == 0) {

                emi = principal / months;

            } else {

                emi = principal * monthlyRate *
                        Math.pow(1 + monthlyRate, months)
                        /
                        (Math.pow(1 + monthlyRate, months) - 1);
            }

            loan.setEmi(
                    Math.round(emi * 100.0) / 100.0
            );
        }

        return loanRepository.save(loan);
    }

    // GET ALL LOANS
    public List<Loan> getAllLoans() {

        return loanRepository.findAll();
    }

    // GET USER LOANS
    public List<Loan> getUserLoans(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return loanRepository.findByUser(user);
    }

    // GET LOAN BY ID
    public Loan getLoanById(Long id) {

        return loanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));
    }

    // UPDATE STATUS
    public Loan updateStatus(
            Long id,
            String status) {

        Loan loan = getLoanById(id);

        loan.setStatus(status);

        return loanRepository.save(loan);
    }
    public boolean isUserOwner(
        Long userId,
        String email) {

    return userRepository.findById(userId)
            .map(user -> user.getEmail().equals(email))
            .orElse(false);
}
}