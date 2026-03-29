package com.example.resume.services;

import com.example.resume.enums.FileFormat;
import com.example.resume.security.SecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class FileService {

    @Autowired
    private SecurityHelper securityHelper;


    public String contentToFile(String htmlResumeContent, FileFormat format, Long resumeId) {
        format = FileFormat.PDF;

        generate_pdf(htmlResumeContent);
        generateFile_name(resumeId, format);

        return "file_url";
    }

    // take the html and convert this into the pdf file
    public byte[] generate_pdf(String htmlContent) {
    }

    // take the formatted html and convert this into the docx
    public byte[] generateDocx(String html) {
        return "";
    }

    public String saveFile(String fileContents, String fileName) {
        // db action
    }

    public String deleteFile(String file_url) {
        // delete file from db

    }

    public String generateFile_name(Long resumeId, FileFormat format) {
        Long userId  = securityHelper.getAuthenticatedUserId();

        String timestamp = DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        String file_name = "resume_" + userId + "_" + resumeId + "_" + timestamp + "." + format;

        return file_name;
    }


    public String getFile(String file_url) {

    }


}


//FileService
//a. contentToFile          → main function, takes rendered HTML + format + resumeId
//converts to PDF or DOCX based on format enum
//calls either generatePdf or generateDocx internally
//                returns file_url
//
//                b. generatePdf            → takes rendered HTML string,
//converts it to PDF file,
//returns saved file path
//
//c. generateDocx           → takes rendered HTML string,
//converts it to DOCX file,
//returns saved file path
//
//d. saveFile               → takes generated file (bytes) + filename,
//saves to disk or cloud storage,
//returns file_url string
//
//e. deleteFile             → takes file_url,
//deletes file from disk or cloud storage,
//called when resume is deleted
//
//f. generateFileName       → generates a unique filename
//e.g. resume_userId_resumeId_timestamp.pdf
//keeps filenames unique and traceable
//
//g. getFile                → takes file_url,
//reads and returns file as byte array,
//used by downloadResume endpoint
//```
//
//        ---
//
//        **The flow between them:**
//        ```
//contentToFile(renderedHtml, format, resumeId)
//        ↓
//generateFileName(userId, resumeId, format)
//        ↓ returns "resume_1_23_1234567890.pdf"
//generatePdf or generateDocx (based on format)
//        ↓ returns file as byte[]
//saveFile(fileBytes, fileName)
//        ↓ saves to disk or cloud
//return file_url