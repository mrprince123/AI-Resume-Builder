# This is an AI Tool
    Title - AI Resume Maker

# Description 
    You just need to give the Job Description (only in english) and select the template + and you can select the format. 
    It will generate resume for you in your desired format. (like, pdf and docx). 

# Workflow. 

    # With Login 
        1. Using will come to home page and login or create the account
        2. After account creation 
        3. They will redirect to the page where they can see all the resumes templates or their previous resumes. 
        4. When they create on the create resume button then, 
        5. Form will appre with Input box to fill the 
            1. Domain/Field (eg. IT, marketting, sales, electronics)
            2. Job Description 
            3. Resume Template Selection 
            4. Type Output Format/Type
        6. After the Creation of the Resume they can be able to down that.
        7. As they are login, then can see and manage their resumes 
        8. They can also manage their profile as well. 
        9. Need to add phone register with opt and google signing as well. 


    #Without Login 
        1. User can see the Home page with Create resume button
        2. A form will appear with some input fields, likes
        3. Form will appre with Input box to fill the 
            1. Domain/Field (eg. IT, marketting, sales, electronics)
            2. Job Description 
            3. Resume Template Selection 
            4. Type Output Format/Type
        4. After the Creation of the Resume they can be able to down that.

# Dev Workflow. 
    1. User submits domain + job description
    2. AIService sends prompt to Gemini → receives structured JSON
    (skills, summary, experience, education, keywords)
    3. ResumeService passes JSON + template_id to TemplateService
    4. TemplateService injects JSON into HTML template
    5. FileService converts HTML → PDF or DOCX
    6. File saved, file_url stored in Resume table
    7. Download link returned to user

# Tech stack
    Backend - Java + Mysql
    Front-end - React.js + Typescript
    AI Model - Google Gemini
    Anyother Free/OpenSource service to generate the resume

# Code Structure

    Database
        1. User table
            a. id
            b. username
            c. name
            d. email
            e. password
            f. status (ACTIVE, INACTIVE)
            g. role (USER, ADMIN) 
            i. is_verified
            k. timestamp

        2. User Details
            a. id
            b. user_id
            b. name
            c. phone_number
            d. location
            e. user_meta
            f. user_ip
            g. auth_provider
            h. google_id
            i. timestamp

        3. Resume Table
            a. id
            b. user_id
            c. tempate_id
            d. content (json format)
            e. format
            f. status (pending, generated, failed)
            g. file_url
            g. timestamp
        
        4. Template Table
            a. id
            b. name
            c. template_preview
            d. html_contents
            e. timestamp

    Services
        1. AuthServices (this service will handle opt and google signing).
        2. ResumeService
        3. TemplateSerive
        4. UserService
        5. AIService
            This AI Service, take all the Job description information and send this to AI and get backend the response in the json format and pass down to the templateSerivce for further render.
        7. FileService
    
    Controller
        1. AuthConroller
        2. ResumeController
        3. TemplateController
        4. UserController


# API Endpoints
    1. Auth 
        login - POST -> /api/v1/login
        register - POST ->  /api/v1/register
        logout - POST ->  /api/v1/logout
        send otp - POST -> /api/v1/auth/send-otp and 
        verify otp - POST -> /api/v1/auth/verify-otp
        google callback - GET -> /api/v1/auth/google/callback
        change password - POST -> /api/v1/auth/change-password
        send password reset link - POST -> /api/v1/auth/reset-password-link

    2. Resume
        get all resume of one user - GET -> /api/v1/resume
        get resume by Id - GET -> /api/v1/resume/{resume_id}
        create - POST -> /api/v1/resume
        update - PUT -> /api/v1/resume/{resume_id}
        delete - DELETE -> /api/v1/resume/{resume_id}
        download - GET -> /api/v1/resume/{resume_id}/download

     3. Template
        get all template - GET -> /api/v1/template
        get template by Id - GET -> /api/v1/template/{template_id}
        create - POST -> /api/v1/template
        update - PUT -> /api/v1/template/{template_id}
        delete - DELETE -> /api/v1/template/{template_id}

    4. User
        get user profile - GET -> /api/v1/profile/{user_id}
        get user meta data - GET -> /api/v1/profile/meta/{user_id}

# Response Structure
    For Servies
        success : true,
        message : "Data Fetched Successfully",
        data : {},
        meta : {}
    
    Error/Validation Response
       {
            "success": false,
            "message": "Invalid email or password",
            "error_code": "AUTH_001",
            "data": null
        }
    

# Future Upgrades
    Give feature to create their own resume by selecting template and adding their information and generating the resume. 


# Services
    These are the full Serives and its functions. 

 Resume Service
    a. getAllResume
    b. getResumeById
    c. createResume
    d. updateResume
    e. deleteResume
    f. downloadResume
    h. generateResume - there should be an explicit function that orchestrates the full pipeline (calls AIService → TemplateService → FileService).

 Auth Service
    a. register
    b. login
    c. logout
    d. sendotp
    f. verifyotp
    g. googlecallback
    h. changePassword
    i. send password reset link
    j. resetPassword
    k. refreshToken


 Template Service
    a. getallTemplate
    b. getTemplateById
    c. createTemplate
    d. updateTemplate
    e. deleteTemplate

 User Service
    a. getUserProfile
    b. UpdateUserProfile
    c. deleteUser (Admin Only)
    d. getAllUserProfile (Admin Only)
    e. updateUserStatus (Admin)


 AI Service
    a. analyseJobDescription
    b. parseAIResponse

 File Service
    a. contentToFile
    b. convertFileType
    c. saveFile
    d. deleteFile


# Middleware
    Auth Middleware
    RateLimit Middleware
    Logging Middleware


# Enum
    Role         → USER, ADMIN
    Status       → ACTIVE, INACTIVE
    ResumeStatus → PENDING, GENERATED, FAILED
    AuthProvider → LOCAL, GOOGLE, PHONE
    FileFormat   → PDF, DOCX


# Helper
    a. Response Helper
    a. Validation Helper
    b. Pagination Helper

