package com.example.resume.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TemplateRequest {
    @NotBlank(message = "name is required")
    @Size(min = 8, message = "name must be at least 8 characters")
    private String name;

    @NotBlank(message = "template preview url is required")
    private String templatePreview;

    @NotBlank(message = "html contents is required")
    private String htmlContents;
}
