package loan_nova_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import loan_nova_backend.entity.LoanApplication;
import loan_nova_backend.service.LoanApplicationService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LoanApplicationService applicationService;

    public AdminController(LoanApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    // View all loan applications
    @GetMapping("/applications")
    public ResponseEntity<List<LoanApplication>> getApplications() {

        return ResponseEntity.ok(
                applicationService.getAllApplications()
        );
    }

    // Approve application
    @PutMapping("/applications/{id}/approve")
    public ResponseEntity<?> approveApplication(
            @PathVariable Long id) {

        try {

            LoanApplication application =
                    applicationService.updateStatus(
                            id,
                            "APPROVED"
                    );

            return ResponseEntity.ok(application);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // Reject application
    @PutMapping("/applications/{id}/reject")
    public ResponseEntity<?> rejectApplication(
            @PathVariable Long id) {

        try {

            LoanApplication application =
                    applicationService.updateStatus(
                            id,
                            "REJECTED"
                    );

            return ResponseEntity.ok(application);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}