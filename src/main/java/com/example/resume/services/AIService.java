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

    // main function called by ResumeService
    public ResumeContent analyseJobDescription(Domain domain, String jobDescription) {

        // 1. call Gemini and get raw response
        String rawResponse = analyseResumeContents(domain, jobDescription);
        log.info("Raw AI response received for domain: {}", domain);

        // 2. parse raw response into ResumeContent
        ResumeContent content = parseAIResponse(rawResponse);

        // 3. validate the parsed content
        validateAIResponse(content);

        return content;
    }

    // send prompt to Gemini and get raw string back
    private String analyseResumeContents(Domain domain, String jobDescription) {

        // initialize client
        Client client = new Client.Builder()
                .apiKey(apiKey)
                .build();

        // build prompt
        String prompt = buildPrompt(domain, jobDescription);

        // send to Gemini
        GenerateContentResponse response = client.models
                .generateContent("gemini-2.0-flash", prompt, null);

        log.info("Gemini response received successfully");

        return response.text();
    }

    // build the prompt string to send to Gemini
    private String buildPrompt(Domain domain, String jobDescription) {
        return """
                You are an expert resume writer with 10+ years of experience
                crafting professional resumes for %s roles.
                
                Your task is to analyze the job description below and generate
                a highly tailored resume content in JSON format.
                
                Follow these rules strictly:
                - summary     : Write a 3-4 sentence professional summary that
                                highlights relevant experience, skills, and value
                                the candidate brings to this specific role.
                - skills      : Extract 8-12 most relevant technical and soft
                                skills directly mentioned or implied in the JD.
                - experience  : Write 4-6 strong bullet points using action verbs
                                (e.g. Developed, Managed, Optimized, Led).
                                Each bullet should highlight impact and results.
                - education   : Suggest the most relevant degree or certification
                                that matches this role.
                - keywords    : Extract 8-10 ATS keywords directly from the JD.
                                These are words a recruiter would search for.
                
                Return ONLY this exact JSON structure with no extra text,
                no markdown, no backticks, no explanation:
                {
                    "summary"    : "professional summary here",
                    "skills"     : ["skill1", "skill2", "skill3"],
                    "experience" : [
                        "Action verb + task + result/impact",
                        "Action verb + task + result/impact"
                    ],
                    "education"  : ["Relevant degree or certification"],
                    "keywords"   : ["keyword1", "keyword2", "keyword3"]
                }
                
                Job Domain      : %s
                Job Description : %s
                """.formatted(domain, domain, jobDescription);
    }

    // parse raw Gemini response string into ResumeContent object
    private ResumeContent parseAIResponse(String rawResponse) {

        // strip markdown backticks if Gemini adds them
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

    // validate that all required fields are present in parsed content
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
