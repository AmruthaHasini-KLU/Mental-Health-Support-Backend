package com.healthsupport.repository;

import com.healthsupport.model.PasswordResetOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, Long> {
    Optional<PasswordResetOTP> findByEmailAndOtp(String email, String otp);
}
