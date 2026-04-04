package com.example.resume.services;

import com.example.resume.entity.payload.ResumeContent;
import com.example.resume.enums.Domain;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ResumeContent analyseJobDescription(Domain domain, String jobDescription, String htmlContent) {
        String rawResponse = analyseResumeContents(domain, jobDescription, htmlContent);
        log.info("Raw AI response received for domain: {}", domain);

        ResumeContent content = parseAIResponse(rawResponse);

        validateAIResponse(content);

        return content;
    }

    private String analyseResumeContents(Domain domain, String jobDescription, String htmlContent) {

        Client client = new Client.Builder()
                .apiKey(apiKey)
                .build();


        String prompt = buildPrompt(domain, jobDescription, htmlContent);

        GenerateContentResponse response = client.models
                .generateContent("gemini-2.0-flash", prompt, null);

        log.info("Gemini response received successfully");

        return response.text();
    }

    private String buildPrompt(Domain domain, String jobDescription, String htmlContent) {
        return """
            You are an expert resume writer with 10+ years of experience
            crafting professional resumes for %s roles.

            Your task is to:
            1. Analyze the provided HTML template structure
            2. Understand how content is organized (sections, headings, lists, etc.)
            3. Generate resume content that BEST FITS into that exact structure

            IMPORTANT:
            - Do NOT return HTML
            - Do NOT modify the template
            - ONLY generate structured JSON data that maps cleanly to the template

            The HTML template defines how the resume is structured.
            You must infer:
            - Section ordering
            - Whether content is short/long
            - Whether lists (<li>) or paragraphs (<p>) are expected
            - How many items fit naturally in each section

            Based on that, generate optimized content.

            Follow these rules strictly:

            - summary:
              Generate content that fits inside paragraph tags (<p>).
              Keep it concise (3-4 sentences max).

            - skills:
              If the template uses lists, generate 8-12 short bullet-friendly skills.
              If compact layout is implied, keep skills short (1-2 words each).

            - experience:
              Generate bullet points that fit inside <li> elements.
              Use strong action verbs (Developed, Led, Optimized, Built).
              Keep each point concise and impact-focused.

            - education:
              Provide 1-2 relevant degrees/certifications.
              Keep formatting clean and short.

            - keywords:
              Extract 8-10 ATS-friendly keywords from the job description.

            Ensure:
            - Content length matches the template design (avoid overflow)
            - Bullet points are clean and scannable
            - No unnecessary verbosity
            - Content aligns with job description

            Return ONLY this JSON (no extra text):
            Based on the htmlContent Structure provided to you %s,

            Job Domain      : %s
            Job Description : %s

            HTML Template:
            %s
            """.formatted(domain, domain, jobDescription, htmlContent);
    }

    private ResumeContent parseAIResponse(String rawResponse) {
        String cleaned = rawResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, ResumeContent.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", rawResponse);
            throw new RuntimeException("AI response parsing failed: " + e.getMessage());
        }
    }

    private void validateAIResponse(ResumeContent content) {

        if (content == null) {
            throw new RuntimeException("AI response is null");
        }
        if (content.getSummary() == null || content.getSummary().isBlank()) {
            throw new RuntimeException("AI response missing summary");
        }
        if (content.getSkills() == null || content.getSkills().isEmpty()) {
            throw new RuntimeException("AI response missing skills");
        }
        if (content.getExperience() == null || content.getExperience().isEmpty()) {
            throw new RuntimeException("AI response missing experience");
        }
        if (content.getEducation() == null || content.getEducation().isEmpty()) {
            throw new RuntimeException("AI response missing education");
        }
        if (content.getKeywords() == null || content.getKeywords().isEmpty()) {
            throw new RuntimeException("AI response missing keywords");
        }

        log.info("AI response validated successfully");
    }
}
