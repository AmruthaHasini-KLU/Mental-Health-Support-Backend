package com.healthsupport.controller;

import com.healthsupport.dto.AuthRequest;
import com.healthsupport.dto.AuthResponse;
import com.healthsupport.dto.RegisterRequest;
import com.healthsupport.dto.ForgotPasswordRequest;
import com.healthsupport.dto.ResetPasswordRequest;
import com.healthsupport.service.PasswordResetService;
import com.healthsupport.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(userService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.processForgotPassword(request);
        return ResponseEntity.ok("OTP has been sent to your email successfully.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.processResetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}
