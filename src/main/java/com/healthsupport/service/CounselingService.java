package com.healthsupport.service;

import com.healthsupport.dto.BookRequestDto;
import com.healthsupport.model.CounselingRequest;
import com.healthsupport.model.DoctorProfile;
import com.healthsupport.model.Status;
import com.healthsupport.model.User;
import com.healthsupport.repository.CounselingRequestRepository;
import com.healthsupport.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselingService {

    private final CounselingRequestRepository counselingRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    public CounselingRequest bookSession(BookRequestDto requestDto, User user) {
        CounselingRequest request = CounselingRequest.builder()
                .issueDescription(requestDto.getIssueDescription())
                .timestamp(LocalDateTime.now())
                .preferredTime(requestDto.getPreferredTime())
                .status(Status.REQUESTED)
                .user(user)
                .build();

        return counselingRepository.save(request);
    }

    public List<CounselingRequest> getUserRequests(User user) {
        return counselingRepository.findByUserOrderByTimestampDesc(user);
    }

    public List<CounselingRequest> getAllRequests() {
        return counselingRepository.findAllByOrderByTimestampDesc();
    }

    public List<CounselingRequest> getUnassignedRequests() {
        return counselingRepository.findByStatusOrderByTimestampDesc(Status.REQUESTED);
    }

    public List<CounselingRequest> getDoctorRequests(User doctor) {
        return counselingRepository.findByDoctorOrderByTimestampDesc(doctor);
    }

    @Transactional
    public CounselingRequest acceptRequest(Long id, User doctor) {
        CounselingRequest request = counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (request.getDoctor() != null) {
            throw new IllegalStateException("Request has already been accepted by another doctor.");
        }

        if (request.getPreferredTime() != null) {
            boolean hasConflict = counselingRepository.existsByDoctorAndPreferredTimeAndStatusIn(
                    doctor,
                    request.getPreferredTime(),
                    Arrays.asList(Status.DOCTOR_ACCEPTED, Status.APPROVED)
            );
            if (hasConflict) {
                throw new IllegalStateException("Schedule conflict: You already have a session at this time.");
            }
        }

        request.setDoctor(doctor);
        request.setStatus(Status.DOCTOR_ACCEPTED);
        return counselingRepository.save(request);
    }

    public CounselingRequest declineRequest(Long id, User doctor) {
        // Declining simply means the doctor doesn't take it.
        // As it is unassigned and broadcasted to all doctors, we don't necessarily update the global request.
        // We will just return the request as is for now.
        return counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }

    @Transactional
    public CounselingRequest approveRequest(Long id) {
        CounselingRequest request = counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        request.setStatus(Status.APPROVED);
        return counselingRepository.save(request);
    }

    @Transactional
    public CounselingRequest assignDoctorManually(Long id, User doctor) {
        CounselingRequest request = counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        request.setDoctor(doctor);
        request.setStatus(Status.APPROVED);
        return counselingRepository.save(request);
    }

    @Transactional
    public CounselingRequest completeSession(Long id) {
        CounselingRequest request = counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        request.setStatus(Status.COMPLETED);
        counselingRepository.save(request);

        if (request.getDoctor() != null) {
            DoctorProfile profile = doctorProfileRepository.findByUser(request.getDoctor())
                    .orElseGet(() -> {
                        DoctorProfile newProfile = DoctorProfile.builder().user(request.getDoctor()).build();
                        return doctorProfileRepository.save(newProfile);
                    });

            profile.setCompletedSessions(profile.getCompletedSessions() + 1);
            profile.setTotalSessions(profile.getTotalSessions() + 1);
            profile.setPoints(profile.getPoints() + 10);
            
            // Simple rating calculation
            float rating = ((float) profile.getCompletedSessions() / profile.getTotalSessions()) * 5.0f;
            profile.setRating(rating);
            
            doctorProfileRepository.save(profile);
        }

        return request;
    }

    public CounselingRequest updateRequestStatus(Long id, Status status) {
        CounselingRequest request = counselingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found with ID: " + id));
        
        request.setStatus(status);
        return counselingRepository.save(request);
    }
}
