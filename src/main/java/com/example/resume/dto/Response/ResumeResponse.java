package com.example.resume.dto.Response;

import com.example.resume.entity.payload.ResumeContent;
import com.example.resume.enums.Domain;
import com.example.resume.enums.FileFormat;
import com.example.resume.enums.ResumeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private Domain domain;
    private String jobDescription;
    private FileFormat format;
    private ResumeStatus status;
    private ResumeContent content;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}