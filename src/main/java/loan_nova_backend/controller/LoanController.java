package loan_nova_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import loan_nova_backend.entity.Loan;
import loan_nova_backend.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // CREATE LOAN
    @PostMapping("/user/{userId}")
    public ResponseEntity<?> createLoan(
            @PathVariable Long userId,
             @Valid @RequestBody Loan loan,
            Authentication authentication) {

        try {

            if (!loanService.isUserOwner(
                    userId,
                    authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to create loan for this user");
            }

            Loan savedLoan =
                    loanService.createLoan(loan, userId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedLoan);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET ALL LOANS
    // Admin ke liye use hoga
    @GetMapping
    public ResponseEntity<List<Loan>> getAllLoans() {

        return ResponseEntity.ok(
                loanService.getAllLoans()
        );
    }

    // GET USER LOANS
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserLoans(
            @PathVariable Long userId,
            Authentication authentication) {

        try {

            if (!loanService.isUserOwner(
                    userId,
                    authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view these loans");
            }

            return ResponseEntity.ok(
                    loanService.getUserLoans(userId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // GET LOAN BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoanById(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            Loan loan = loanService.getLoanById(id);

            if (!loan.getUser()
                    .getEmail()
                    .equals(authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view this loan");
            }

            return ResponseEntity.ok(loan);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // UPDATE LOAN STATUS
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        try {

            Loan loan = loanService.getLoanById(id);

            if (!loan.getUser()
                    .getEmail()
                    .equals(authentication.getName())) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to update this loan");
            }

            return ResponseEntity.ok(
                    loanService.updateStatus(id, status)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}