package loan_nova_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import loan_nova_backend.dto.DashboardResponse;
import loan_nova_backend.repository.UserRepository;
import loan_nova_backend.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(
            DashboardService dashboardService,
            UserRepository userRepository) {

        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDashboard(
            @PathVariable Long userId,
            org.springframework.security.core.Authentication authentication) {

        boolean isOwner =
                userRepository.findById(userId)
                        .map(user ->
                                user.getEmail()
                                        .equals(authentication.getName()))
                        .orElse(false);

        if (!isOwner) {

            return ResponseEntity.status(403)
                    .body("You are not authorized to view this dashboard");
        }

        try {

            DashboardResponse response =
                    dashboardService.getUserDashboard(userId);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}