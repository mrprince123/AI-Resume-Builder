package com.example.resume.services;

import com.example.resume.dto.Request.ResumeRequest;
import com.example.resume.dto.Request.UpdateResumeRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.dto.Response.ResumeResponse;
import com.example.resume.entity.Resume;
import com.example.resume.entity.Template;
import com.example.resume.entity.User;
import com.example.resume.entity.payload.ResumeContent;
import com.example.resume.enums.ResumeStatus;
import com.example.resume.repository.ResumeRepository;
import com.example.resume.repository.TemplateRepository;
import com.example.resume.security.SecurityHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @Autowired
    private AIService aiService;

    @Autowired
    private TemplateService templateService;

    // create
    public ApiResponse<ResumeResponse> create(ResumeRequest request, User user) {

        // 1. validate - check template exists
        Template template = templateRepository.findByTemplateId(request.getTemplateId()).orElseThrow(() -> new RuntimeException("Template not found: " + request.getTemplateId()));

        // 2. check delicacy - same domain + job description already exists for this user
        Resume existingResume = resumeRepository
                .findByUserIdAndDomainAndJobDescription(
                        user.getId(),
                        request.getDomain(),
                        request.getJobDescription()
                );

        if (existingResume != null) {
            return ApiResponse.<ResumeResponse>builder()
                    .status("failed")
                    .message("Resume with the same job description already exists")
                    .data(null)
                    .build();
        }

        // 3. save initial record in DB with PENDING status
        Resume resume = new Resume();
        resume.setFormat(request.getFormat());
        resume.setJobDescription(request.getJobDescription());
        resume.setTemplate(template);
        resume.setDomain(request.getDomain());
        resume.setResumeStatus(ResumeStatus.PENDING);
        resume.setFileUrl(null);
        resume.setContent(null);
        resume.setUser(user);

        resumeRepository.save(resume);

        log.info("Resume record created with PENDING status, id: {}", resume.getId());

        // 4. trigger AI generation pipeline
        try {
            generateResume(resume);
            log.info("Resume generated successfully, id: {}", resume.getId());
        } catch (Exception e) {
            log.error("Resume generation failed, id: {}, error: {}",
                    resume.getId(), e.getMessage());
            return ApiResponse.<ResumeResponse>builder()
                    .status("failed")
                    .message("Resume generation failed, please try again")
                    .data(null)
                    .build();
        }

        // 5. build and return response
        ResumeResponse resumeResponse = ResumeResponse.builder()
                .id(resume.getId())
                .domain(resume.getDomain())
                .jobDescription(resume.getJobDescription())
                .format(resume.getFormat())
                .status(resume.getResumeStatus())
                .content(resume.getContent())
                .fileUrl(resume.getFileUrl())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume created successfully")
                .data(resumeResponse)
                .build();
    }

    // generate resume - performs the full AI pipeline
    private void generateResume(Resume resume){
        try {
            // 1. send job description to Gemini → get structured JSON back
            log.info("Starting AI analysis for resume id: {}", resume.getId());
            ResumeContent content = aiService.analyseJobDescription(
                    resume.getDomain(),
                    resume.getJobDescription()
            );
            log.info("AI analysis completed for resume id: {}", resume.getId());

            // 2. inject content into HTML template → get rendered HTML string
            log.info("Rendering template for resume id: {}", resume.getId());
            String renderedHtml = templateService.renderTemplate(
                    resume.getTemplate(),
                    content
            );
            log.info("Template rendered successfully for resume id: {}", resume.getId());

            // 3. convert rendered HTML → PDF or DOCX file → get file_url back
            log.info("Generating file for resume id: {}", resume.getId());
            String fileUrl = fileService.contentToFile(
                    renderedHtml,
                    resume.getFormat(),
                    resume.getId()
            );
            log.info("File generated successfully for resume id: {}", resume.getId());

            // 4. update resume record → GENERATED
            resume.setContent(content);
            resume.setFileUrl(fileUrl);
            resume.setResumeStatus(ResumeStatus.GENERATED);
            resumeRepository.save(resume);
            log.info("Resume record updated to GENERATED, id: {}", resume.getId());

        } catch (Exception e) {
            resume.setResumeStatus(ResumeStatus.FAILED);
            resume.setContent(null);
            resume.setFileUrl(null);
            resumeRepository.save(resume);
            log.error("Resume generation failed for id: {}, error: {}",
                    resume.getId(), e.getMessage());

            throw new RuntimeException("Resume generation failed: " + e.getMessage());
        }
    }

    // re-generate resume
    public ApiResponse<ResumeResponse> regenerateResume(Long resumeId, User user) {

        // find resume
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        // check it belongs to this user
        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to resume: " + resumeId);
        }

        // check status is FAILED
        if (resume.getResumeStatus() != ResumeStatus.FAILED) {
            return ApiResponse.<ResumeResponse>builder()
                    .status("failed")
                    .message("Only failed resumes can be regenerated")
                    .data(null)
                    .build();
        }

        // reset to PENDING
        resume.setResumeStatus(ResumeStatus.PENDING);
        resumeRepository.save(resume);
        log.info("Resume reset to PENDING for regeneration, id: {}", resumeId);

        // trigger generation pipeline again
        GenerateResumeResult result = generateResume(resume);

        // build and return response
        ResumeResponse resumeResponse = mapToResponse(resume);
        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resume regenerated successfully")
                .data(resumeResponse)
                .build();
    }

    // get all resume
    public ApiResponse<List<ResumeResponse>> getAllResume(){

        List<Resume> resumes = resumeRepository.findAll();

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .domain(resume.getDomain())
                        .jobDescription(resume.getJobDescription())
                        .format(resume.getFormat())
                        .status(resume.getResumeStatus())
                        .content(resume.getContent())
                        .fileUrl(resume.getFileUrl())
                        .createdAt(resume.getCreatedAt())
                        .updatedAt(resume.getUpdatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());


        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("All Resumes fetched successfully")
                .data(resumeResponses)
                .build();
    }

    // get all resume by User id
    public ApiResponse<List<ResumeResponse>> getAllResumeByUser(User user){

        List<Resume> resumes = resumeRepository.findResumeByUser(user);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .domain(resume.getDomain())
                        .jobDescription(resume.getJobDescription())
                        .format(resume.getFormat())
                        .status(resume.getResumeStatus())
                        .content(resume.getContent())
                        .fileUrl(resume.getFileUrl())
                        .createdAt(resume.getCreatedAt())
                        .updatedAt(resume.getUpdatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("All Resumes fetched successfully by user " + user)
                .data(resumeResponses)
                .build();
    }

    // get my resume
    public ApiResponse<List<ResumeResponse>> getMyResumes(){

        Long userId  = securityHelper.getAuthenticatedUserId();

        List<Resume> resumes = resumeRepository.findMyResume(userId);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .domain(resume.getDomain())
                        .jobDescription(resume.getJobDescription())
                        .format(resume.getFormat())
                        .status(resume.getResumeStatus())
                        .content(resume.getContent())
                        .fileUrl(resume.getFileUrl())
                        .createdAt(resume.getCreatedAt())
                        .updatedAt(resume.getUpdatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("All My Resumes fetched successfully")
                .data(resumeResponses)
                .build();
    }

    // get resume by resumeId
    public ApiResponse<ResumeResponse> getResumeById(Long id){

        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        ResumeResponse resumeResponse = ResumeResponse.builder()
                .id(resume.getId())
                .domain(resume.getDomain())
                .jobDescription(resume.getJobDescription())
                .format(resume.getFormat())
                .status(resume.getResumeStatus())
                .content(resume.getContent())
                .fileUrl(resume.getFileUrl())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();

        log.info("Resume fetched successfully, by provided id: {}", resume.getId());

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resumes fetched successfully by provided id " +  id)
                .data(resumeResponse)
                .build();
    }

    // get resume by Status
    public ApiResponse<List<ResumeResponse>> getResumeByStatus(ResumeStatus status){

        List<Resume> resumes = resumeRepository.findByStatus(status);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(resume -> ResumeResponse.builder()
                        .id(resume.getId())
                        .domain(resume.getDomain())
                        .jobDescription(resume.getJobDescription())
                        .format(resume.getFormat())
                        .status(resume.getResumeStatus())
                        .content(resume.getContent())
                        .fileUrl(resume.getFileUrl())
                        .createdAt(resume.getCreatedAt())
                        .updatedAt(resume.getUpdatedAt())
                        .build()
                )
                .collect(Collectors.toList());

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return ApiResponse.<List<ResumeResponse>>builder()
                .status("success")
                .message("All Resumes fetched successfully by status " + status)
                .data(resumeResponses)
                .build();
    }

    // delete resume
    public ApiResponse<Void> deleteResume(Long id){
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));
        resumeRepository.delete(resume);

        log.info("Resume deleted successfully, by provided id: {}", resume.getId());

        return ApiResponse.<Void>builder()
                .status("success")
                .message("Resumes deleted successfully with provided id"+ id)
                .data(null)
                .build();
    }

    // update resume (edit resume)
    public ApiResponse<ResumeResponse> updateResume(UpdateResumeRequest request){
        Resume resume = resumeRepository.findById(request.getResumeId()).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        // validate and update each input
        if (request.getDomain() != null) {
            resume.setDomain(request.getDomain());
        }

        if (request.getJobDescription() != null) {
            resume.setJobDescription(request.getJobDescription());
        }

        if (request.getTemplateId() != null) {
            resume.setTemplate(request.getTemplateId());
        }

        if (request.getFormat() != null) {
            resume.setFormat(request.getFormat());
        }

        resumeRepository.save(resume);

        ResumeResponse resumeResponse = ResumeResponse.builder()
                .id(resume.getId())
                .domain(resume.getDomain())
                .jobDescription(resume.getJobDescription())
                .format(resume.getFormat())
                .status(resume.getResumeStatus())
                .content(resume.getContent())
                .fileUrl(resume.getFileUrl())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();

        log.info("Resume updated successfully, by provided id: {}", resume.getId());

        return ApiResponse.<ResumeResponse>builder()
                .status("success")
                .message("Resumes updated successfully with provided id "+ request.getResumeId())
                .data(resumeResponse)
                .build();
    }

    // download resume here call the file service to give the download url
    public ApiResponse<String> downloadResume(Long id){
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        // call the file service and pass the resume to download
        log.info("Resume downloaded successfully, by provided id: {}", resume.getId());

        return ApiResponse.<String>builder()
                .status("success")
                .message("Resumes downloaded successfully with provided id"+ id)
                .data("www.goog.eom")
                .build();


        // give the ApiResponse as return
    }
}
