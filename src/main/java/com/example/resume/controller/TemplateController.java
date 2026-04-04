package com.example.resume.controller;

import com.example.resume.dto.Request.TemplateRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.entity.Template;
import com.example.resume.services.TemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Template", description = "Endpoints for managing resume templates")
@RestController
@RequestMapping("/api/v1/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @PostMapping("/")
    private ApiResponse<String> createTemplate(@RequestBody TemplateRequest request) {
        String templateName = templateService.createTemplate(request);

        // return the response.
        return ApiResponse.<String>builder()
                .status("200")
                .message("Template created successfully")
                .data(templateName)
                .build();
    }

    @DeleteMapping("/{id}")
    private ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);

        // return the response.
        return ApiResponse.<Void>builder()
                .status("success")
                .message("Template with id deleted successfully" + id)
                .data(null)
                .build();
    }

    @PutMapping("/{id}")
    private ApiResponse<Template> updateTemplate(@PathVariable Long id, @RequestBody TemplateRequest request) {
        Template template = templateService.updateTemplate(id, request);

        // return the response.
        return ApiResponse.<Template>builder()
                .status("success")
                .message("Template with id deleted successfully" + id)
                .data(template)
                .build();
    }

    @GetMapping("/")
    private ApiResponse<List<Template>> getAllTemplate() {
        List<Template> templates = templateService.getAllTemplate();

        // return the response.
        return ApiResponse.<List<Template>>builder()
                .status("success")
                .message("Templates fetched successfully")
                .data(templates)
                .build();
    }

    @GetMapping("/{id}")
    private ApiResponse<Template> createTemplate(@PathVariable Long id) {
        Template template = templateService.getTemplateById(id);

        // return the response.
        return ApiResponse.<Template>builder()
                .status("success")
                .message("Template with provided id fetched successfully" + id)
                .data(template)
                .build();
    }
}
