package com.healthsupport.dto;

import com.healthsupport.model.Role;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String name;
    private String email;
    private Role role;
}
