package com.shotdo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shotdo.backend.entity.Verification;
import com.shotdo.backend.entity.User;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByUserAndDate(User user, LocalDate date);
}
