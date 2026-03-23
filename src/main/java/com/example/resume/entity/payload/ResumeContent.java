package com.example.resume.entity.payload;


import lombok.Data;

import java.util.List;

@Data
public class ResumeContent {
    private String summary;
    private List<String> skills;
    private List<String> experience;
    private List<String> education;
    private List<String> keywords;
}
