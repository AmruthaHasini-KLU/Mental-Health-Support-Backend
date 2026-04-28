package com.healthsupport.dto;

import com.healthsupport.model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDashboardDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private LocalDateTime lastLogin;
    private int loginCount;
    private boolean isActive;
}
