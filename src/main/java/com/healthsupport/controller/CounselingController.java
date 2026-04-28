package com.healthsupport.controller;

import com.healthsupport.dto.BookRequestDto;
import com.healthsupport.model.CounselingRequest;
import com.healthsupport.model.User;
import com.healthsupport.service.CounselingService;
import com.healthsupport.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
public class CounselingController {

    private final CounselingService counselingService;
    private final UserService userService;

    @PostMapping("/book")
    public ResponseEntity<CounselingRequest> bookSession(@Valid @RequestBody BookRequestDto requestDto, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getCurrentUserEntity(email);
        return new ResponseEntity<>(counselingService.bookSession(requestDto, currentUser), HttpStatus.CREATED);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<CounselingRequest>> getMyRequests(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getCurrentUserEntity(email);
        return ResponseEntity.ok(counselingService.getUserRequests(currentUser));
    }
}
