package com.example.resume.repository;

import com.example.resume.entity.Resume;
import com.example.resume.entity.User;
import com.example.resume.enums.ResumeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Resume findb
    List<Resume> findMyResume(Long userId);

    List<Resume> findResumeByUser(User user);

    List<Resume> findByStatus(ResumeStatus status);
}
