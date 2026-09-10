package loan_nova_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import loan_nova_backend.entity.Loan;
import loan_nova_backend.entity.LoanApplication;
import loan_nova_backend.entity.User;
import loan_nova_backend.repository.LoanApplicationRepository;
import loan_nova_backend.repository.LoanRepository;
import loan_nova_backend.repository.UserRepository;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public LoanApplicationService(
            LoanApplicationRepository applicationRepository,
            UserRepository userRepository,
            LoanRepository loanRepository) {

        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    // CREATE APPLICATION
    public LoanApplication createApplication(
            LoanApplication application,
            Long userId,
            Long loanId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() ->
                        new RuntimeException("Loan not found"));

        application.setUser(user);
        application.setLoan(loan);

        if (application.getStatus() == null ||
                application.getStatus().isBlank()) {

            application.setStatus("PENDING");
        }

        // Interest rate loan se automatically lena
        if (application.getInterestRate() == null) {
            application.setInterestRate(
                    loan.getInterestRate()
            );
        }

        // EMI calculation
        if (application.getRequestedAmount() != null &&
                application.getInterestRate() != null &&
                application.getTenure() != null) {

            double principal = application.getRequestedAmount();
            double annualRate = application.getInterestRate();
            int months = application.getTenure();

            double monthlyRate =
                    annualRate / 12 / 100;

            double emi;

            if (monthlyRate == 0) {

                emi = principal / months;

            } else {

                emi = principal * monthlyRate *
                        Math.pow(1 + monthlyRate, months)
                        /
                        (Math.pow(1 + monthlyRate, months) - 1);
            }

            application.setEmi(
                    Math.round(emi * 100.0) / 100.0
            );
        }

        return applicationRepository.save(application);
    }

    // GET ALL APPLICATIONS
    public List<LoanApplication> getAllApplications() {

        return applicationRepository.findAll();
    }

    // GET USER APPLICATIONS
    public List<LoanApplication> getUserApplications(
            Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return applicationRepository.findByUser(user);
    }

    // GET APPLICATION BY ID
    public LoanApplication getApplicationById(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Application not found"));
    }

    // GET APPLICATIONS BY STATUS
    public List<LoanApplication> getApplicationsByStatus(
            String status) {

        return applicationRepository.findByStatus(status);
    }

    // UPDATE APPLICATION STATUS
    public LoanApplication updateStatus(
            Long id,
            String status) {

        LoanApplication application =
                getApplicationById(id);

        String normalizedStatus =
                status.toUpperCase();

        if (!normalizedStatus.equals("PENDING") &&
            !normalizedStatus.equals("APPROVED") &&
            !normalizedStatus.equals("REJECTED")) {

            throw new RuntimeException(
                    "Invalid status. Use PENDING, APPROVED or REJECTED"
            );
        }

        application.setStatus(normalizedStatus);

        return applicationRepository.save(application);
    }

    // DELETE APPLICATION
    public void deleteApplication(Long id) {

        LoanApplication application =
                getApplicationById(id);

        applicationRepository.delete(application);
    }
    public boolean isUserOwner(
        Long userId,
        String email) {

    return userRepository.findById(userId)
            .map(user -> user.getEmail().equals(email))
            .orElse(false);
}
}