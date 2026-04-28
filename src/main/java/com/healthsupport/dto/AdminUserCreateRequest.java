package com.healthsupport.dto;

import com.healthsupport.model.Role;
import lombok.Data;

@Data
public class AdminUserCreateRequest {
    private String name;
    private String email;
    private String password;
    private Role role;
}
