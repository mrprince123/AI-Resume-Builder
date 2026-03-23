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
    private TemplateService  templateService;


    @PostMapping("/")
    private ApiResponse<String> createTemplate(@RequestBody TemplateRequest request){
        return  templateService.createTemplate(request);
    }

    @DeleteMapping("/{id}")
    private ApiResponse<Void> deleteTemplate(@PathVariable Long id){
        return  templateService.deleteTemplate(id);
    }

    @PutMapping("/{id}")
    private ApiResponse<String> updateTemplate(@PathVariable Long id, @RequestBody TemplateRequest request){
        return  templateService.updateTemplate(id, request);
    }

    @GetMapping("/")
    private ApiResponse<List<Template>> getAllTemplate(){
        return  templateService.getAllTemplate();
    }

    @GetMapping("/{id}")
    private ApiResponse<Template> createTemplate(@PathVariable Long id){
        return  templateService.getTemplateById(id);
    }

}
