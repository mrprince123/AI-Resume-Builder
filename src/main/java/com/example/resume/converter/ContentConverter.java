package com.example.resume.converter;


import com.example.resume.entity.payload.ResumeContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ContentConverter implements AttributeConverter<ResumeContent, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ResumeContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            throw new RuntimeException("Error converting content to JSON", e);
        }
    }

    @Override
    public ResumeContent convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, ResumeContent.class);
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to content", e);
        }
    }
}