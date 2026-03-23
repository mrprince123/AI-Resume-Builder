package com.example.resume.services;

public class AIService {

    //1. AnalyseJobDescription
    // main function, takes domain + job description, builds the prompt, send to gemini return the resumeContent

//    b. buildPrompt                → builds the prompt string to send to Gemini
//    takes domain + job description as input
//    returns a formatted prompt string


//    c. parseAIResponse            → takes raw Gemini response string,
//    validates and parses it into
//    ResumeContent object

//    d. validateAIResponse         → checks if the parsed ResumeContent
//    has all required fields (summary, skills etc.)
//            throws exception if invalid


//    **The flow between them:**
//            ```
//    analyseJobDescription(domain, jobDescription)
//        ↓
//    buildPrompt(domain, jobDescription)
//        ↓ returns prompt string
//    Send prompt to Gemini API
//        ↓ returns raw JSON string
//    parseAIResponse(rawResponse)
//        ↓ returns ResumeContent object
//    validateAIResponse(resumeContent)
//        ↓ confirms all fields are present
//return ResumeContent

    private String analyseResumeContents(){

        return "Working";
    }

}
