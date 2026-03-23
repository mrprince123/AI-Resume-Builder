package com.example.resume.entity;

import com.example.resume.converter.ContentConverter;
import com.example.resume.entity.payload.ResumeContent;
import com.example.resume.enums.Domain;
import com.example.resume.enums.FileFormat;
import com.example.resume.enums.ResumeStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Table(name = "resume")
@Entity
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", columnDefinition = "VARCHAR(20) DEFAULT 'IT'")
    private Domain domain;

    @Column(name = "job_description", columnDefinition = "LONGTEXT")
    private String jobDescription;

    @Convert(converter = ContentConverter.class)
    @Column(name = "content", columnDefinition = "JSON")
    private ResumeContent content;

    @Column(name = "file_format", columnDefinition = "VARCHAR(20) DEFAULT 'PDF'")
    private FileFormat format = FileFormat.PDF;

    @Enumerated(EnumType.STRING)
    @Column(name = "resume_status", columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    private ResumeStatus resumeStatus = ResumeStatus.PENDING;

    @Column(nullable = false, name = "file_url")
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
