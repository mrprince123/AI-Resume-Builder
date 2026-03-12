package com.example.resume.repository;


import com.example.resume.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String username);

    User findByEmail(String email);

    boolean existsByUserName(String username);

    boolean existsByEmail(String email);

    Optional<User> findOptionalByUserName(String username);

    Optional<User> findOptionalByEmail(String email);


}
