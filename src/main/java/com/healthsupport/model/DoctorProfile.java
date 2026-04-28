package com.healthsupport.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String specialization;

    @Builder.Default
    private int points = 0;

    @Builder.Default
    private float rating = 0.0f;

    @Builder.Default
    private int totalSessions = 0;

    @Builder.Default
    private int completedSessions = 0;

    @Builder.Default
    private int experience = 0;
}
