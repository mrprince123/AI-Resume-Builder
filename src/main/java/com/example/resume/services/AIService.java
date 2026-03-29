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

    public ResumeContent analyseJobDescription(Domain domain, String jobDescription) {
        String rawResponse = analyseResumeContents(domain, jobDescription);
        log.info("Raw AI response received for domain: {}", domain);

        ResumeContent content = parseAIResponse(rawResponse);

        validateAIResponse(content);

        return content;
    }

    private String analyseResumeContents(Domain domain, String jobDescription) {

        Client client = new Client.Builder()
                .apiKey(apiKey)
                .build();


        String prompt = buildPrompt(domain, jobDescription);

        GenerateContentResponse response = client.models
                .generateContent("gemini-2.0-flash", prompt, null);

        log.info("Gemini response received successfully");

        return response.text();
    }

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
