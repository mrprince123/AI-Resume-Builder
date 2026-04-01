package com.example.resume.repository;

import com.example.resume.entity.Resume;
import com.example.resume.entity.User;
import com.example.resume.enums.Domain;
import com.example.resume.enums.ResumeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserId(Long userId);

    List<Resume> findResumeByUser(User user);

    List<Resume> findByResumeStatus(ResumeStatus status);

    Resume findByUserIdAndDomainAndJobDescription(Long userId, Domain Domain, String JobDescription);
}
