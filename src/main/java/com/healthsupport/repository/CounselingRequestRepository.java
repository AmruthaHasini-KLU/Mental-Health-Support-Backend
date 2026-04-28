package com.healthsupport.repository;

import com.healthsupport.model.CounselingRequest;
import com.healthsupport.model.Status;
import com.healthsupport.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CounselingRequestRepository extends JpaRepository<CounselingRequest, Long> {
    List<CounselingRequest> findByUserOrderByTimestampDesc(User user);
    List<CounselingRequest> findAllByOrderByTimestampDesc();
    
    // Fetch all requests that are purely REQUESTED
    List<CounselingRequest> findByStatusOrderByTimestampDesc(Status status);

    // Check if doctor has a session booked at this exact time (overlap logic)
    boolean existsByDoctorAndPreferredTimeAndStatusIn(User doctor, LocalDateTime preferredTime, List<Status> statuses);

    // Fetch requests assigned to a specific doctor
    List<CounselingRequest> findByDoctorOrderByTimestampDesc(User doctor);
}
