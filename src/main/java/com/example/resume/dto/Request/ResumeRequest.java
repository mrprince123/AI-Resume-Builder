package com.example.resume.dto.Request;

import com.example.resume.entity.Template;
import com.example.resume.enums.Domain;
import com.example.resume.enums.FileFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResumeRequest {
    @NotBlank(message = "domain is required")
    private Domain domain;

    @NotBlank(message = "Job description is required")
    @Size(min = 100, max = 5000, message = "Job description must be between 100 and 5000 characters")
    private String jobDescription;

    @NotNull(message = "Template is required")
    private Long templateId;

    private FileFormat format;
}
