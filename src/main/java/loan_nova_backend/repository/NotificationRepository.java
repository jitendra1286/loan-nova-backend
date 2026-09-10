package loan_nova_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import loan_nova_backend.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);
}