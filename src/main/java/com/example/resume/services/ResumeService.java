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
import com.example.resume.repository.UserRepository;
import com.example.resume.security.SecurityHelper;
import jakarta.security.auth.message.AuthException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityHelper securityHelper;

    @Autowired
    private AIService aiService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FileService fileService;

    // create
    @Transactional
    public ResumeResponse create(ResumeRequest request) {

        // 1. validate - check template exists
        Template template = templateRepository.findById(request.getTemplateId()).orElseThrow(() -> new RuntimeException("Template not found: " + request.getTemplateId()));

        Long userId = securityHelper.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // 2. check delicacy - same domain + job description already exists for this user
        Resume existingResume = resumeRepository
                .findByUserIdAndDomainAndJobDescription(
                        userId,
                        request.getDomain(),
                        request.getJobDescription()
                );

        if (existingResume != null) {
            throw new RuntimeException("Resume with the same job description already exists");
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

            throw new RuntimeException("Resume generation failed");
        }

        return mapToResponse(resume);
    }

    // generate resume - performs the full AI pipeline
    @Async
    @Transactional
    public void generateResume(Resume resume){
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
    @Transactional
    public ResumeResponse regenerateResume(Long resumeId) {

        // find resume
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        Long userId = securityHelper.getAuthenticatedUserId();

        // check it belongs to this user
        if (!resume.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to resume: " + resumeId);
        }

        // check status is FAILED
        if (resume.getResumeStatus() != ResumeStatus.FAILED) {
            throw new RuntimeException("Only failed resumes can be regenerated");
        }

        // reset to PENDING
        resume.setResumeStatus(ResumeStatus.PENDING);
        resumeRepository.save(resume);
        log.info("Resume reset to PENDING for regeneration, id: {}", resumeId);

        generateResume(resume);

        log.info("Resume generated successfully, id: {}", resume.getId());

        return mapToResponse(resume);
    }

    // get all resume
    public List<ResumeResponse> getAllResume(){

        List<Resume> resumes = resumeRepository.findAll();

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return resumeResponses;
    }

    // get all resume by User id
    public List<ResumeResponse> getAllResumeByUser(Long userId){

        // here you want find the user from the userId
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with provided " + userId));

        List<Resume> resumes = resumeRepository.findResumeByUser(user);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return resumeResponses;
    }

    // get my resume
    public List<ResumeResponse> getMyResumes(){

        Long userId  = securityHelper.getAuthenticatedUserId();

        List<Resume> resumes = resumeRepository.findByUserId(userId);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return resumeResponses;
    }

    // get resume by resumeId
    public ResumeResponse getResumeById(Long id){
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        log.info("Resume fetched successfully, by provided id: {}", resume.getId());

        return mapToResponse(resume);
    }

    // get resume by Status
    public List<ResumeResponse> getResumeByStatus(ResumeStatus status){

        List<Resume> resumes = resumeRepository.findByResumeStatus(status);

        // map Resume entity → ResumeResponse
        List<ResumeResponse> resumeResponses = resumes.stream()
                .map(this::mapToResponse)
                .toList();

        log.info("All resumes fetched successfully, count: {}", resumeResponses.size());

        return resumeResponses;
    }

    // delete resume
    public void deleteResume(Long id) throws IOException {
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        Long userId = securityHelper.getAuthenticatedUserId();

        if (!resume.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to resume: " + id);
        }

        resumeRepository.delete(resume);

        // Then delete file (best effort)
        if (resume.getFileUrl() != null) {
            try {
                fileService.deleteFile(resume.getFileUrl());
            } catch (Exception e) {
                log.error("File deletion failed for resume id: {}, error: {}", id, e.getMessage());
            }
        }

        log.info("Resume deleted successfully, by provided id: {}", resume.getId());
    }

    // update resume (edit resume)
    public ResumeResponse updateResume(UpdateResumeRequest request) {
        Long currentUserId  = securityHelper.getAuthenticatedUserId();

        Resume resume = resumeRepository.findById(request.getResumeId()).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        // Add auth check here
        if (!resume.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized");
        }

        // validate and update each input
        if (request.getDomain() != null) {
            resume.setDomain(request.getDomain());
        }

        if (request.getJobDescription() != null) {
            resume.setJobDescription(request.getJobDescription());
        }

        if (request.getTemplateId() != null) {
            Template template = templateRepository.findById(request.getTemplateId()).orElseThrow(() -> new RuntimeException("No template found with provided Id" + request.getTemplateId()));
            resume.setTemplate(template);
        }

        if (request.getFormat() != null) {
            resume.setFormat(request.getFormat());
        }

        resumeRepository.save(resume);

        log.info("Resume updated successfully, by provided id: {}", resume.getId());

        return mapToResponse(resume);
    }

    // download resume here call the file service to give the download url
    public String downloadResume(Long id){
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new RuntimeException("No Resume found with Provided Id"));

        Long userId = securityHelper.getAuthenticatedUserId();

        if (!resume.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        log.info("Resume downloaded successfully, by provided id: {}", resume.getId());

        if (resume.getFileUrl() == null) {
            throw new RuntimeException("Resume file not generated yet");
        }

        return resume.getFileUrl();
    }


    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
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
    }


}
