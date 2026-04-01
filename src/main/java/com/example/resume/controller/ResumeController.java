package com.example.resume.controller;

import com.example.resume.dto.Request.ResumeRequest;
import com.example.resume.dto.Request.UpdateResumeRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.ResumeResponse;
import com.example.resume.entity.User;
import com.example.resume.enums.ResumeStatus;
import com.example.resume.services.ResumeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Resume", description = "Endpoints for managing resumes")
@RestController
@RequestMapping("/api/v1/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/")
    public ApiResponse<ResumeResponse> crateResume(@RequestBody ResumeRequest request){
          return resumeService.create(request);
    }

    @GetMapping("/regenerate/{resumeId}")
    public ApiResponse<ResumeResponse> regenerateResume(@PathVariable Long resumeId){
        return resumeService.regenerateResume(resumeId);
    }


    @GetMapping("/")
    public ApiResponse<List<ResumeResponse>> getAllResume(@RequestBody ResumeRequest request){
        return resumeService.getAllResume();
    }


    @GetMapping("/myresume")
    public ApiResponse<List<ResumeResponse>> getMyResume(@RequestBody ResumeRequest request){
        return resumeService.getMyResumes();
    }

    @GetMapping("/user/resume")
    public ApiResponse<List<ResumeResponse>> getAllResumeByUser(@RequestBody User user){
        return resumeService.getAllResumeByUser(user);
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeResponse> getResumeById(@PathVariable Long id){
        return resumeService.getResumeById(id);
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<ResumeResponse>> getResumeByStatus(@PathVariable ResumeStatus status){
        return resumeService.getResumeByStatus(status);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResume(@PathVariable Long id) throws IOException {
        return resumeService.deleteResume(id);
    }

    @PutMapping("/")
    public ApiResponse<ResumeResponse> updateResume(@RequestBody UpdateResumeRequest request) {
        return resumeService.updateResume(request);
    }

    @GetMapping("/download/{id}")
    public ApiResponse<String> downloadResume(@PathVariable Long id) {
        return resumeService.downloadResume(id);
    }

}
