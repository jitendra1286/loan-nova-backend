package loan_nova_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import loan_nova_backend.entity.LoanApplication;
import loan_nova_backend.service.LoanApplicationService;

@RestController
@RequestMapping("/api/applications")
public class LoanApplicationController {

    private final LoanApplicationService applicationService;

    public LoanApplicationController(
            LoanApplicationService applicationService) {

        this.applicationService = applicationService;
    }

    // CREATE APPLICATION
    @PostMapping("/user/{userId}/loan/{loanId}")
    public ResponseEntity<?> createApplication(
            @PathVariable Long userId,
            @PathVariable Long loanId,
            @Valid @RequestBody LoanApplication application,
            Authentication authentication) {

        try {

            // User can only create application
            // for their own account
            if (!applicationService.isUserOwner(
                    userId,
                    authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to create application for this user");
            }

            LoanApplication saved =
                    applicationService.createApplication(
                            application,
                            userId,
                            loanId
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(saved);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET ALL APPLICATIONS
    // Normally this should be ADMIN only.
    @GetMapping
    public ResponseEntity<List<LoanApplication>>
    getAllApplications(
            Authentication authentication) {

        /*
         * ADMIN check
         */
        if (!hasAdminRole(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        return ResponseEntity.ok(
                applicationService.getAllApplications()
        );
    }

    // GET USER APPLICATIONS
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserApplications(
            @PathVariable Long userId,
            Authentication authentication) {

        try {

            if (!applicationService.isUserOwner(
                    userId,
                    authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view these applications");
            }

            return ResponseEntity.ok(
                    applicationService
                            .getUserApplications(userId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET APPLICATION BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            LoanApplication application =
                    applicationService
                            .getApplicationById(id);

            // Only owner can view
            if (!application.getUser()
                    .getEmail()
                    .equals(authentication.getName())) {

                // Admin can also view
                if (!hasAdminRole(authentication)) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body("You are not authorized to view this application");
                }
            }

            return ResponseEntity.ok(application);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // GET APPLICATIONS BY STATUS
    // ADMIN ONLY
    @GetMapping("/status")
    public ResponseEntity<?> getByStatus(
            @RequestParam String status,
            Authentication authentication) {

        if (!hasAdminRole(authentication)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Admin access required");
        }

        return ResponseEntity.ok(
                applicationService
                        .getApplicationsByStatus(status)
        );
    }

    // UPDATE STATUS
    // ADMIN ONLY
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        try {

            if (!hasAdminRole(authentication)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Admin access required");
            }

            return ResponseEntity.ok(
                    applicationService.updateStatus(
                            id,
                            status
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // DELETE APPLICATION
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            LoanApplication application =
                    applicationService
                            .getApplicationById(id);

            boolean owner =
                    application.getUser()
                            .getEmail()
                            .equals(authentication.getName());

            boolean admin =
                    hasAdminRole(authentication);

            if (!owner && !admin) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to delete this application");
            }

            applicationService.deleteApplication(id);

            return ResponseEntity.ok(
                    "Application deleted successfully"
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // ADMIN ROLE CHECK
    private boolean hasAdminRole(
            Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"));
    }
}