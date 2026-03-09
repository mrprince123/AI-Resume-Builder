# AI Resume Maker

## Description

Provide a Job Description (in English only), select a template, and choose your output format. The tool will generate a resume in your desired format (PDF or DOCX).

---

## Workflow

### With Login

1. User arrives at the home page and logs in or creates an account.
2. After account creation, they are redirected to a dashboard showing all resume templates and their previously created resumes.
3. Clicking **Create Resume** opens a form with the following fields:
   - Domain / Field (e.g. IT, Marketing, Sales, Electronics)
   - Job Description
   - Resume Template Selection
   - Output Format (PDF / DOCX)
4. After generation, the user can download the resume.
5. Logged-in users can view, manage, and delete their resumes.
6. Users can also manage their profile.
7. Supports phone registration with OTP and Google Sign-In.

### Without Login

1. User sees the home page with a **Create Resume** button.
2. Clicking it opens a form with the following fields:
   - Domain / Field (e.g. IT, Marketing, Sales, Electronics)
   - Job Description
   - Resume Template Selection
   - Output Format (PDF / DOCX)
3. After generation, the user can download the resume.

---

## Dev Workflow

1. User submits domain + job description.
2. `AIService` sends a prompt to Gemini → receives structured JSON (skills, summary, experience, education, keywords).
3. `ResumeService` passes JSON + `template_id` to `TemplateService`.
4. `TemplateService` injects JSON into the HTML template.
5. `FileService` converts HTML → PDF or DOCX.
6. File is saved, and `file_url` is stored in the Resume table.
7. Download link is returned to the user.

---

## Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Backend    | Java + MySQL                            |
| Frontend   | React.js + TypeScript                   |
| AI Model   | Google Gemini                           |
| File Gen   | Open-source HTML → PDF/DOCX library     |

---

## Code Structure

### Database

#### 1. User Table

| Field        | Details                  |
|--------------|--------------------------|
| `id`         | Primary Key              |
| `username`   |                          |
| `name`       |                          |
| `email`      |                          |
| `password`   |                          |
| `status`     | `ACTIVE`, `INACTIVE`     |
| `role`       | `USER`, `ADMIN`          |
| `is_verified`|                          |
| `timestamp`  |                          |

#### 2. User Details Table

| Field           | Details                          |
|-----------------|----------------------------------|
| `id`            | Primary Key                      |
| `user_id`       | Foreign Key → User               |
| `name`          |                                  |
| `phone_number`  |                                  |
| `location`      |                                  |
| `user_meta`     | JSON                             |
| `user_ip`       |                                  |
| `auth_provider` | `local`, `google`, `phone`       |
| `google_id`     |                                  |
| `timestamp`     |                                  |

#### 3. Resume Table

| Field        | Details                            |
|--------------|------------------------------------|
| `id`         | Primary Key                        |
| `user_id`    | Foreign Key → User                 |
| `template_id`| Foreign Key → Template             |
| `content`    | JSON format                        |
| `format`     | `pdf`, `docx`                      |
| `status`     | `pending`, `generated`, `failed`   |
| `file_url`   |                                    |
| `timestamp`  |                                    |

#### 4. Template Table

| Field              | Details      |
|--------------------|--------------|
| `id`               | Primary Key  |
| `name`             |              |
| `template_preview` | Image URL    |
| `html_content`     | HTML string  |
| `timestamp`        |              |

---

### Services

| Service          | Responsibility                                                                                      |
|------------------|------------------------------------------------------------------------------------------------------|
| `AuthService`    | Handles login, registration, OTP, and Google Sign-In                                                |
| `ResumeService`  | Manages resume creation, retrieval, update, and deletion                                            |
| `TemplateService`| Injects AI-generated JSON into HTML templates                                                       |
| `UserService`    | Manages user profile and details                                                                    |
| `AIService`      | Sends job description to Gemini, receives structured JSON, passes it to `TemplateService` for rendering |
| `FileService`    | Converts rendered HTML to PDF or DOCX, saves file, and returns `file_url`                          |

---

### Controllers

- `AuthController`
- `ResumeController`
- `TemplateController`
- `UserController`

---

## API Endpoints

### 1. Auth

| Method | Endpoint                          | Description               |
|--------|-----------------------------------|---------------------------|
| POST   | `/api/v1/auth/login`              | Login                     |
| POST   | `/api/v1/auth/register`           | Register                  |
| POST   | `/api/v1/auth/logout`             | Logout                    |
| POST   | `/api/v1/auth/send-otp`           | Send OTP                  |
| POST   | `/api/v1/auth/verify-otp`         | Verify OTP                |
| GET    | `/api/v1/auth/google/callback`    | Google OAuth Callback     |
| POST   | `/api/v1/auth/change-password`    | Change Password           |
| POST   | `/api/v1/auth/reset-password-link`| Send Password Reset Link  |

### 2. Resume

| Method | Endpoint                              | Description                  |
|--------|---------------------------------------|------------------------------|
| GET    | `/api/v1/resume`                      | Get all resumes (auth user)  |
| GET    | `/api/v1/resume/{resume_id}`          | Get resume by ID             |
| POST   | `/api/v1/resume`                      | Create resume                |
| PUT    | `/api/v1/resume/{resume_id}`          | Update resume                |
| DELETE | `/api/v1/resume/{resume_id}`          | Delete resume                |
| GET    | `/api/v1/resume/{resume_id}/download` | Download resume file         |

### 3. Template

| Method | Endpoint                              | Description              |
|--------|---------------------------------------|--------------------------|
| GET    | `/api/v1/template`                    | Get all templates        |
| GET    | `/api/v1/template/{template_id}`      | Get template by ID       |
| POST   | `/api/v1/template`                    | Create template (Admin)  |
| PUT    | `/api/v1/template/{template_id}`      | Update template (Admin)  |
| DELETE | `/api/v1/template/{template_id}`      | Delete template (Admin)  |

### 4. User

| Method | Endpoint                          | Description          |
|--------|-----------------------------------|----------------------|
| GET    | `/api/v1/profile/{user_id}`       | Get user profile     |
| GET    | `/api/v1/profile/meta/{user_id}`  | Get user metadata    |

---

## Response Structure

### Success Response

```json
{
  "success": true,
  "message": "Data fetched successfully",
  "data": {},
  "meta": {
    "page": 1,
    "per_page": 10,
    "total": 45,
    "total_pages": 5
  }
}
```

### Error / Validation Response

```json
{
  "success": false,
  "message": "Invalid email or password",
  "error_code": "AUTH_001",
  "data": null
}
```

---

## Future Upgrades

- Allow users to build their own resume by manually selecting a template and filling in their information.
- IP-based rate limiting for guest (unauthenticated) resume generation.
- Resume scoring against the provided job description.
- Cover letter generation.
- Multi-language support.
