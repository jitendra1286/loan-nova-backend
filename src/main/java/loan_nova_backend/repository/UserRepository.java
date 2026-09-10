package loan_nova_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import loan_nova_backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}