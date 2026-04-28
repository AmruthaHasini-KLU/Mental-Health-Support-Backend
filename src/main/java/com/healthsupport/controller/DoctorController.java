package com.healthsupport.controller;

import com.healthsupport.model.CounselingRequest;
import com.healthsupport.model.User;
import com.healthsupport.service.CounselingService;
import com.healthsupport.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    private final CounselingService counselingService;
    private final UserService userService;

    @GetMapping("/requests")
    public ResponseEntity<List<CounselingRequest>> getAvailableRequests() {
        return ResponseEntity.ok(counselingService.getUnassignedRequests());
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<CounselingRequest>> getMyRequests(Authentication authentication) {
        User doctor = userService.getCurrentUserEntity(authentication.getName());
        return ResponseEntity.ok(counselingService.getDoctorRequests(doctor));
    }

    @PutMapping("/accept/{id}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User doctor = userService.getCurrentUserEntity(authentication.getName());
            return ResponseEntity.ok(counselingService.acceptRequest(id, doctor));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/decline/{id}")
    public ResponseEntity<CounselingRequest> declineRequest(@PathVariable Long id, Authentication authentication) {
        User doctor = userService.getCurrentUserEntity(authentication.getName());
        return ResponseEntity.ok(counselingService.declineRequest(id, doctor));
    }
    
    @PutMapping("/complete/{id}")
    public ResponseEntity<CounselingRequest> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(counselingService.completeSession(id));
    }
}
