package com.healthsupport.service;

import com.healthsupport.dto.ForgotPasswordRequest;
import com.healthsupport.dto.ResetPasswordRequest;
import com.healthsupport.model.PasswordResetOTP;
import com.healthsupport.model.User;
import com.healthsupport.repository.PasswordResetOTPRepository;
import com.healthsupport.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOTPRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void processForgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            // To prevent email enumeration attacks, do not throw error. 
            // Just return or you can throw if you don't care about it.
            // Requirement specifies "send to user's email", so if user doesn't exist, we skip.
            throw new IllegalArgumentException("User with this email not found");
        }

        String otp = generateNumericOTP(6);
        
        PasswordResetOTP resetOTP = PasswordResetOTP.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
                
        otpRepository.save(resetOTP);
        
        // Send email
        String subject = "HealthSupport - Your Password Reset OTP";
        String body = "Your OTP for resetting the password is : " + otp + "\n" +
                "It is valid for 5 minutes.";
        
        emailService.sendEmail(request.getEmail(), subject, body);
    }
    
    public void processResetPassword(ResetPasswordRequest request) {
        // Validate OTP
        PasswordResetOTP resetOTP = otpRepository.findByEmailAndOtp(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));
                
        if (resetOTP.isUsed()) {
            throw new IllegalArgumentException("OTP has already been used");
        }
        
        if (resetOTP.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        
        // Fetch user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        // Encrypt new password and save
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Mark OTP as used
        resetOTP.setUsed(true);
        otpRepository.save(resetOTP);
    }

    private String generateNumericOTP(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
