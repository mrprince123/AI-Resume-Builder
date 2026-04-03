package com.example.resume.controller;

import com.example.resume.dto.Request.ResumeRequest;
import com.example.resume.dto.Request.UpdateResumeRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.ResumeResponse;
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
          ResumeResponse response = resumeService.create(request);

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume created successfully")
                .data(response)
                .build();
    }

    @PostMapping("/{resumeId}/regenerate")
    public ApiResponse<ResumeResponse> regenerateResume(@PathVariable Long resumeId){
        ResumeResponse response= resumeService.regenerateResume(resumeId);

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume regenerated successfully")
                .data(response)
                .build();
    }

    @GetMapping("/")
    public ApiResponse<List<ResumeResponse>> getAllResume(){
        List<ResumeResponse> response = resumeService.getAllResume();

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("Resume created successfully")
                .data(response)
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<List<ResumeResponse>> getMyResume(@RequestBody ResumeRequest request){
        List<ResumeResponse> response = resumeService.getMyResumes();

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("Your Resume fetched successfully")
                .data(response)
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ResumeResponse>> getAllResumeByUser(@PathVariable Long userId){
        List<ResumeResponse> response  = resumeService.getAllResumeByUser(userId);

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("Resume regenerated successfully")
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ResumeResponse> getResumeById(@PathVariable Long id){

        ResumeResponse response  = resumeService.getResumeById(id);

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume fetched by Id successfully")
                .data(response)
                .build();
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<ResumeResponse>> getResumeByStatus(@PathVariable ResumeStatus status){
        List<ResumeResponse> responses = resumeService.getResumeByStatus(status);

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("Resume regenerated successfully")
                .data(responses)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResume(@PathVariable Long id) throws IOException {
        resumeService.deleteResume(id);

        return ApiResponse.<Void>builder()
                .status("success")
                .message("Resume deleted successfully")
                .data(null)
                .build();
    }

    @PutMapping("/")
    public ApiResponse<ResumeResponse> updateResume(@RequestBody UpdateResumeRequest request) {
        ResumeResponse response = resumeService.updateResume(request);

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume deleted successfully")
                .data(response)
                .build();
    }

    @GetMapping("/{id}/download")
    public ApiResponse<String> downloadResume(@PathVariable Long id) {
        String resumeUrl = resumeService.downloadResume(id);

        return ApiResponse.<String>builder()
                .status("success")
                .message("Resumes downloaded successfully with provided id"+ id)
                .data(resumeUrl)
                .build();
    }

}
