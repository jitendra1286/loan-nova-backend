package loan_nova_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import loan_nova_backend.entity.Repayment;
import loan_nova_backend.service.RepaymentService;

@RestController
@RequestMapping("/api/repayments")
public class RepaymentController {

    private final RepaymentService repaymentService;

    public RepaymentController(
            RepaymentService repaymentService) {

        this.repaymentService = repaymentService;
    }

    // GENERATE EMI SCHEDULE
    @PostMapping("/loan/{loanId}/generate")
    public ResponseEntity<?> generateSchedule(
            @PathVariable Long loanId) {

        try {

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            repaymentService
                                    .generateSchedule(loanId)
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET LOAN REPAYMENTS
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getLoanRepayments(
            @PathVariable Long loanId) {

        try {

            return ResponseEntity.ok(
                    repaymentService
                            .getLoanRepayments(loanId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET USER REPAYMENTS
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRepayments(
            @PathVariable Long userId,
            Authentication authentication) {

        try {

            if (!isOwner(userId, authentication)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                            "You are not authorized to view these repayments"
                        );
            }

            return ResponseEntity.ok(
                    repaymentService
                            .getUserRepayments(userId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET SINGLE REPAYMENT
    @GetMapping("/{id}")
    public ResponseEntity<?> getRepayment(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            Repayment repayment =
                    repaymentService
                            .getRepaymentById(id);

            if (!repayment.getLoan()
                    .getUser()
                    .getEmail()
                    .equals(
                            authentication.getName()
                    )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                            "You are not authorized to view this repayment"
                        );
            }

            return ResponseEntity.ok(repayment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // MARK EMI AS PAID
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false)
            String paymentReference,
            Authentication authentication) {

        try {

            Repayment repayment =
                    repaymentService
                            .getRepaymentById(id);

            if (!repayment.getLoan()
                    .getUser()
                    .getEmail()
                    .equals(
                            authentication.getName()
                    )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                            "You are not authorized to pay this EMI"
                        );
            }

            return ResponseEntity.ok(
                    repaymentService.markAsPaid(
                            id,
                            paymentReference
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // REPAYMENT SUMMARY
    @GetMapping("/loan/{loanId}/summary")
    public ResponseEntity<?> getSummary(
            @PathVariable Long loanId,
            Authentication authentication) {

        try {

            Repayment repayment =
                    repaymentService
                            .getLoanRepayments(loanId)
                            .stream()
                            .findFirst()
                            .orElse(null);

            if (repayment == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                            "Repayment schedule not generated"
                        );
            }

            if (!repayment.getLoan()
                    .getUser()
                    .getEmail()
                    .equals(
                            authentication.getName()
                    )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                            "You are not authorized to view this summary"
                        );
            }

            return ResponseEntity.ok(
                    repaymentService
                            .getSummary(loanId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    private boolean isOwner(
            Long userId,
            Authentication authentication) {

        return authentication != null &&
                authentication.getName() != null &&
                repaymentService
                        .getUserRepayments(userId)
                        .stream()
                        .anyMatch(repayment ->
                                repayment.getLoan()
                                        .getUser()
                                        .getEmail()
                                        .equals(
                                                authentication.getName()
                                        )
                        );
    }
}