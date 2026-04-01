package com.example.resume.services;

import com.example.resume.dto.Request.TemplateRequest;
import com.example.resume.dto.Response.ApiResponse;
import com.example.resume.entity.Template;
import com.example.resume.entity.payload.ResumeContent;
import com.example.resume.repository.TemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    // create template
    public ApiResponse<String> createTemplate(TemplateRequest request) {

        Template templateExists = templateRepository.findByName(request.getName());

        // find if the template with the same name already exits
        if (templateExists != null) {
            return ApiResponse.<String>builder()
                    .status("404")
                    .message("Template with this name already exits")
                    .data(null)
                    .build();
        }

        // if not then create one
        Template template = new Template();
        template.setName(request.getName());
        template.setTemplatePreview(request.getTemplatePreview());
        template.setHtmlContents(request.getHtmlContents());
        templateRepository.save(template);

        log.info("New Template created successfully: {}", request.getName());

        // return the response.
        return ApiResponse.<String>builder()
                .status("200")
                .message("Template created successfully")
                .data(template.getName())
                .build();
    }

    // delete template
    public ApiResponse<Void> deleteTemplate(long id) {

        // find if the template with the same name already exits
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));

        templateRepository.delete(template);

        log.info("Template deleted successfully: {}", id);

        // return the response.
        return ApiResponse.<Void>builder()
                .status("success")
                .message("Template with id deleted successfully" + id)
                .data(null)
                .build();

    }

    // update template
    public ApiResponse<String> updateTemplate(long id, TemplateRequest request) {

        // find if the template with the same name already exits
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));


        if (request.getName() != null) {
            template.setName(request.getName());
        }

        if (request.getTemplatePreview() != null) {
            template.setName(request.getTemplatePreview());
        }


        if (request.getHtmlContents() != null) {
            template.setName(request.getHtmlContents());
        }

        templateRepository.save(template);
        log.info("Template updated successfully: {}", id);

        // return the response.
        return ApiResponse.<String>builder()
                .status("success")
                .message("Template with id deleted successfully" + id)
                .data(String.valueOf(template))
                .build();

    }

    // get all template
    public ApiResponse<List<Template>> getAllTemplate() {

        List<Template> templates = templateRepository.findAll();


        log.info("All templates fetched successfully, count: {}", templates.size());

        // return the response.
        return ApiResponse.<List<Template>>builder()
                .status("success")
                .message("Templates fetched successfully")
                .data(templates)
                .build();
    }

    // get Template byId
    public ApiResponse<Template> getTemplateById(long id) {

        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found: " + id));

        log.info("Template fetched successfully: {}", id);

        // return the response.
        return ApiResponse.<Template>builder()
                .status("success")
                .message("Template with provided id fetched successfully" + id)
                .data(template)
                .build();
    }

    public String renderTemplate(Template template, ResumeContent content) {
        try {
            String html = template.getHtmlContents();

            if (html == null || html.isBlank()) {
                throw new RuntimeException("Template HTML content is empty, id: " + template.getId());
            }

            html = html.replace("{{summary}}", content.getSummary());
            html = html.replace("{{skills}}", formatList(content.getSkills()));
            html = html.replace("{{experience}}", formatList(content.getExperience()));
            html = html.replace("{{education}}", formatList(content.getEducation()));
            html = html.replace("{{keywords}}", formatList(content.getKeywords()));

            log.info("Template rendered successfully, id: {}", template.getId());

            return html;

        } catch (Exception e) {
            log.error("Template rendering failed, id: {}, error: {}",
                    template.getId(), e.getMessage());
            throw new RuntimeException("Template rendering failed: " + e.getMessage());
        }
    }

    private String formatList(List<String> items) {

        if (items == null || items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append("<li>").append(item).append("</li>");
        }
        return sb.toString();
    }


}
