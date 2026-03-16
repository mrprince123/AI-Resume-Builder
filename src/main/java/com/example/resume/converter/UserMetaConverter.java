package com.example.resume.converter;

import com.example.resume.entity.UserMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserMetaConverter implements AttributeConverter<UserMeta, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(UserMeta userMeta) {
        if (userMeta == null) return null;
        try {
            return objectMapper.writeValueAsString(userMeta);  // UserMeta → JSON string
        } catch (Exception e) {
            throw new RuntimeException("Error converting UserMeta to JSON", e);
        }
    }

    @Override
    public UserMeta convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, UserMeta.class);  // JSON string → UserMeta
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to UserMeta", e);
        }
    }
}