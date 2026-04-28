package com.healthsupport.service;

import com.healthsupport.dto.AuthRequest;
import com.healthsupport.dto.AuthResponse;
import com.healthsupport.dto.RegisterRequest;
import com.healthsupport.model.User;
import com.healthsupport.repository.UserRepository;
import com.healthsupport.repository.DoctorProfileRepository;
import com.healthsupport.model.DoctorProfile;
import com.healthsupport.model.Role;
import com.healthsupport.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already taken");
        }

        boolean isApproved = request.getRole() != Role.DOCTOR;

        User user = User.builder()
                .name(request.getName())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .approved(isApproved)
                .build();

        user = userRepository.save(user);

        if (request.getRole() == Role.DOCTOR) {
            DoctorProfile profile = DoctorProfile.builder()
                    .user(user)
                    .specialization(request.getSpecialization() != null ? request.getSpecialization() : "General Therapist")
                    .experience(request.getExperience() != null ? request.getExperience() : 0)
                    .build();
            doctorProfileRepository.save(profile);
        }

        // Load spring security Userdetails
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String jwtToken = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordMatches) {
            // Migrate legacy plaintext passwords to BCrypt on first successful login.
            if (request.getPassword().equals(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("Invalid email or password");
            }
        }

        if (!user.isApproved()) {
            throw new IllegalArgumentException("Account pending admin approval");
        }

        user.setLastLogin(java.time.LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        org.springframework.security.core.userdetails.UserDetails userDetails = 
                org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String jwtToken = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
    
    public User getCurrentUserEntity(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public java.util.List<com.healthsupport.dto.UserDashboardDto> getAdminDashboardUsers(com.healthsupport.model.Role roleFilter) {
        java.util.List<User> users;
        if (roleFilter != null) {
            users = userRepository.findByRoleOrderByLastLoginDesc(roleFilter);
        } else {
            users = userRepository.findAllByOrderByLastLoginDesc();
        }

        java.time.LocalDateTime twentyFourHoursAgo = java.time.LocalDateTime.now().minusHours(24);

        return users.stream().map(user -> com.healthsupport.dto.UserDashboardDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .loginCount(user.getLoginCount())
                .isActive(user.getLastLogin() != null && user.getLastLogin().isAfter(twentyFourHoursAgo))
                .build()
        ).collect(java.util.stream.Collectors.toList());
    }

    public com.healthsupport.dto.UserDashboardDto createUser(com.healthsupport.dto.AdminUserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);
        return mapToUserDashboardDto(user);
    }

    public com.healthsupport.dto.UserDashboardDto updateUser(Long id, com.healthsupport.dto.AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already taken");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());

        user = userRepository.save(user);
        return mapToUserDashboardDto(user);
    }

    public void approveDoctor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRole() != Role.DOCTOR) {
            throw new IllegalArgumentException("Only doctors can be explicitly approved this way");
        }
        user.setApproved(true);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    private com.healthsupport.dto.UserDashboardDto mapToUserDashboardDto(User user) {
        java.time.LocalDateTime twentyFourHoursAgo = java.time.LocalDateTime.now().minusHours(24);
        return com.healthsupport.dto.UserDashboardDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .loginCount(user.getLoginCount())
                .isActive(user.getLastLogin() != null && user.getLastLogin().isAfter(twentyFourHoursAgo))
                .build();
    }
}
