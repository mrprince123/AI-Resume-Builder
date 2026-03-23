package com.example.resume.services;

public class FileService {
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