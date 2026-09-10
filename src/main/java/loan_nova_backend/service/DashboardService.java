package loan_nova_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import loan_nova_backend.dto.DashboardResponse;
import loan_nova_backend.entity.Loan;
import loan_nova_backend.entity.LoanApplication;
import loan_nova_backend.entity.Repayment;
import loan_nova_backend.entity.User;
import loan_nova_backend.repository.LoanApplicationRepository;
import loan_nova_backend.repository.LoanRepository;
import loan_nova_backend.repository.RepaymentRepository;
import loan_nova_backend.repository.UserRepository;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final LoanApplicationRepository applicationRepository;
    private final RepaymentRepository repaymentRepository;
    private final NotificationService notificationService;

    public DashboardService(
            UserRepository userRepository,
            LoanRepository loanRepository,
            LoanApplicationRepository applicationRepository,
            RepaymentRepository repaymentRepository,
            NotificationService notificationService) {

        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
        this.applicationRepository = applicationRepository;
        this.repaymentRepository = repaymentRepository;
        this.notificationService = notificationService;
    }

    public DashboardResponse getUserDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        DashboardResponse response = new DashboardResponse();

        response.setUserId(userId);

        // -------------------------
        // LOANS
        // -------------------------

        List<Loan> loans =
                loanRepository.findByUser(user);

        long totalLoans = loans.size();

        long activeLoans = loans.stream()
                .filter(loan ->
                        "ACTIVE".equalsIgnoreCase(loan.getStatus())
                        || "APPROVED".equalsIgnoreCase(loan.getStatus()))
                .count();

        long pendingLoans = loans.stream()
                .filter(loan ->
                        "PENDING".equalsIgnoreCase(loan.getStatus()))
                .count();

        long approvedLoans = loans.stream()
                .filter(loan ->
                        "APPROVED".equalsIgnoreCase(loan.getStatus()))
                .count();

        long rejectedLoans = loans.stream()
                .filter(loan ->
                        "REJECTED".equalsIgnoreCase(loan.getStatus()))
                .count();

        double totalLoanAmount = loans.stream()
                .filter(loan -> loan.getAmount() != null)
                .mapToDouble(Loan::getAmount)
                .sum();

        response.setTotalLoans(totalLoans);
        response.setActiveLoans(activeLoans);
        response.setPendingLoans(pendingLoans);
        response.setApprovedLoans(approvedLoans);
        response.setRejectedLoans(rejectedLoans);
        response.setTotalLoanAmount(
                round(totalLoanAmount)
        );

        // -------------------------
        // APPLICATIONS
        // -------------------------

        List<LoanApplication> applications =
                applicationRepository.findByUser(user);

        long totalApplications = applications.size();

        long pendingApplications = applications.stream()
                .filter(application ->
                        "PENDING".equalsIgnoreCase(
                                application.getStatus()))
                .count();

        long approvedApplications = applications.stream()
                .filter(application ->
                        "APPROVED".equalsIgnoreCase(
                                application.getStatus()))
                .count();

        long rejectedApplications = applications.stream()
                .filter(application ->
                        "REJECTED".equalsIgnoreCase(
                                application.getStatus()))
                .count();

        response.setTotalApplications(totalApplications);
        response.setPendingApplications(pendingApplications);
        response.setApprovedApplications(approvedApplications);
        response.setRejectedApplications(rejectedApplications);

        // -------------------------
        // REPAYMENTS / EMI
        // -------------------------

        List<Repayment> repayments =
                repaymentRepository.findByLoanUserId(userId);

        long totalEmis = repayments.size();

        long paidEmis = repayments.stream()
                .filter(repayment ->
                        "PAID".equalsIgnoreCase(
                                repayment.getStatus()))
                .count();

        long pendingEmis =
                totalEmis - paidEmis;

        double totalPaidAmount = repayments.stream()
                .filter(repayment ->
                        "PAID".equalsIgnoreCase(
                                repayment.getStatus()))
                .filter(repayment ->
                        repayment.getAmount() != null)
                .mapToDouble(Repayment::getAmount)
                .sum();

        double totalRepaymentAmount = repayments.stream()
                .filter(repayment ->
                        repayment.getAmount() != null)
                .mapToDouble(Repayment::getAmount)
                .sum();

        double remainingAmount =
                totalRepaymentAmount - totalPaidAmount;

        response.setTotalEmis(totalEmis);
        response.setPaidEmis(paidEmis);
        response.setPendingEmis(pendingEmis);

        response.setTotalPaidAmount(
                round(totalPaidAmount)
        );

        response.setRemainingAmount(
                round(remainingAmount)
        );

        // -------------------------
        // NOTIFICATIONS
        // -------------------------

        long unreadNotifications =
                notificationService.getUnreadCount(userId);

        response.setUnreadNotifications(
                unreadNotifications
        );

        return response;
    }

    private double round(double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}