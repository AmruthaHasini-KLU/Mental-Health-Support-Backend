package com.healthsupport.repository;

import com.healthsupport.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    java.util.List<User> findAllByOrderByLastLoginDesc();
    java.util.List<User> findByRoleOrderByLastLoginDesc(com.healthsupport.model.Role role);
}
