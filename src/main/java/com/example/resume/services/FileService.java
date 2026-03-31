package com.example.resume.services;

import com.aspose.words.LoadFormat;
import com.aspose.words.LoadOptions;
import com.aspose.words.SaveFormat;
import com.example.resume.enums.FileFormat;
import com.example.resume.security.SecurityHelper;
import com.ironsoftware.ironpdf.PdfDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.aspose.words.Document;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private SecurityHelper securityHelper;

    public String contentToFile(String htmlResumeContent, FileFormat format, Long resumeId) {

        if (htmlResumeContent == null || htmlResumeContent.isBlank()) {
            throw new RuntimeException("HTML content is null or empty");
        }

        String fileName = generateFileName(resumeId, format);

        byte[] fileBytes;
        if (format == FileFormat.PDF) {
            fileBytes = generatePdf(htmlResumeContent);
        } else {
            fileBytes = generateDocx(htmlResumeContent);
        }

        try {
            return saveFile(fileBytes, fileName);
        } catch (IOException e) {
            log.error("Failed to save file for resume id: {}", resumeId);
            throw new RuntimeException("Failed to save file: " + e.getMessage());
        }
    }

    public byte[] generatePdf(String htmlContent) {
        try {
            PdfDocument pdf = PdfDocument.renderHtmlAsPdf(htmlContent);

            byte[] file = pdf.getBinaryData();

            log.info("PDF created successfully");

            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateDocx(String htmlContent) {
        try {
            LoadOptions loadOptions = new LoadOptions();
            loadOptions.setLoadFormat(LoadFormat.HTML);

            Document doc = new Document(
                    new ByteArrayInputStream(htmlContent.getBytes()),
                    loadOptions);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.save(outputStream, SaveFormat.DOCX);

            byte[] file = outputStream.toByteArray();

            log.info("DOCX created successfully");

            return file;

        } catch (Exception e) {
            log.error("DOCX generation failed: {}", e.getMessage());
            throw new RuntimeException("DOCX generation failed: " + e.getMessage());
        }
    }

    public String saveFile(byte[] fileContents, String fileName) throws IOException {

        String storeDirectory = "/uploads/resumes";

        File directory = new File(storeDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
            log.info("File Directory created successfully: {}", directory);
        }

        String filePath = storeDirectory + "/" + fileName;

        Files.write(Paths.get(filePath), fileContents);

        log.info("File saved successfully: {}", filePath);

        return filePath;

    }

    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            log.warn("File path is null or empty, skipping delete");
            return;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.warn("File not found, skipping delete: {}", filePath);
            return;
        }

        Files.delete(path);

        log.info("File deleted successfully: {}", filePath);
    }

    public String generateFileName(Long resumeId, FileFormat format) {
        Long userId = securityHelper.getAuthenticatedUserId();

        String timestamp = DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        return "resume_" + userId + "_" + resumeId + "_" + timestamp + "." + format;
    }

    public byte[] getFile(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            log.error("File path is null or empty");
            throw new RuntimeException("File path is null or empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            log.error("File not found: {}", filePath);
            throw new RuntimeException("File not found: " + filePath);
        }

        byte[] fileBytes = Files.readAllBytes(path);

        log.info("File retrieved successfully: {}", filePath);

        return fileBytes;

    }

}