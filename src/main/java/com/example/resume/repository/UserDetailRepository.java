package com.example.resume.repository;

import com.example.resume.entity.User;
import com.example.resume.entity.UserProfileDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailRepository extends JpaRepository<UserProfileDetails, Long> {

    UserProfileDetails findByUser(User user);

}
