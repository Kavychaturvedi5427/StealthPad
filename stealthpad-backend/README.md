# StealthPad Backend

The StealthPad backend is a Spring Boot REST API for account management, JWT authentication, note storage and synchronization, password recovery, account deletion, and AI-assisted note operations.

The backend is the server-side component of the StealthPad project. The Android application stores and encrypts notes locally, while this service provides authenticated account and synchronization capabilities. The API persists note content in PostgreSQL, so production deployments must protect the database and all application secrets appropriately.

## Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Building and Testing](#building-and-testing)
- [Running with Docker](#running-with-docker)
- [Authentication](#authentication)
- [API Reference](#api-reference)
- [Data Model](#data-model)
- [Note Synchronization](#note-synchronization)
- [Error Responses](#error-responses)
- [Security Behavior](#security-behavior)
- [Email and Password Recovery](#email-and-password-recovery)
- [AI Features](#ai-features)
- [Database Behavior](#database-behavior)
- [Troubleshooting](#troubleshooting)
- [Development Notes](#development-notes)

## Features

- User registration and login.
- Stateless JWT-based authentication.
- BCrypt password hashing.
- Password reset through a time-limited, six-digit email OTP.
- Authenticated note creation, listing, updating, deletion, and bulk deletion.
- Incremental note synchronization between clients and the server.
- Per-user data isolation for notes.
- Account deletion with associated note deletion.
- Health endpoint for service status checks.
- AI-powered note summarization.
- AI-powered note generation and restructuring.
- AI-powered key-point extraction.
- Consistent JSON error responses for validation, authentication, authorization, conflicts, missing resources, and service failures.
- Thymeleaf email templating for password-reset messages.

## Technology Stack

| Area | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 4.0.5 |
| Web | Spring MVC through `spring-boot-starter-webmvc` |
| Persistence | Spring Data JPA and Hibernate |
| Database | PostgreSQL |
| Security | Spring Security with stateless JWT authentication |
| JWT | JJWT 0.13.0 |
| Password hashing | BCrypt |
| Object mapping | ModelMapper 3.2.5 |
| Email delivery | Resend Java SDK 4.19.0 |
| Email templates | Thymeleaf |
| AI integration | Spring AI 2.0.1 with Google Gemini |
| HTTP client | OkHttp 4.12.0 |
| Validation | Spring Boot validation starter |
| Build tool | Maven Wrapper |
| Container image | Eclipse Temurin 25 JDK |
| Test framework | JUnit 5 through Spring Boot test starters |

## Project Structure

```text
stealthpad-backend/
├── pom.xml                         Maven project and dependency definition
├── mvnw                            Unix Maven Wrapper script
├── mvnw.cmd                        Windows Maven Wrapper script
├── Dockerfile                      Container build and startup instructions
├── HELP.md                         Spring Initializr reference links
├── .mvn/wrapper/                   Maven Wrapper files
├── src/
│   ├── main/
│   │   ├── java/com/kavya/stealthpad/
│   │   │   ├── StealthpadBackendApplication.java
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── StealthAuthController.java
│   │   │   │   ├── StealthAiController.java
│   │   │   │   ├── StealthNotesController.java
│   │   │   │   └── UserController.java
│   │   │   ├── Dto/                  Request and response objects
│   │   │   ├── Entity/               JPA entities for users and notes
│   │   │   ├── exception/             Exceptions and global error handling
│   │   │   ├── repository/            Spring Data JPA repositories
│   │   │   ├── security/              JWT and request-security components
│   │   │   ├── service/               Business logic and integrations
│   │   │   └── utils/                 Mapping helpers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/forgot-password.html
│   └── test/
│       └── java/com/kavya/stealthpad/stealthpad/backend/
│           └── StealthpadBackendApplicationTests.java
└── target/                           Generated build output; do not commit
```

### Main application

`StealthpadBackendApplication` is the Spring Boot entry point. It starts component scanning and application auto-configuration.

### Configuration

`AppConfig` provides the `ModelMapper`, BCrypt `PasswordEncoder`, and `AuthenticationManager` beans.

### Controllers

- `StealthAuthController` exposes registration, login, and password recovery endpoints.
- `StealthNotesController` exposes note CRUD and synchronization endpoints.
- `StealthAiController` exposes summarization, note generation, and key-point extraction.
- `UserController` exposes account deletion.
- `HealthController` exposes service health information.

### Services

- `AuthService` handles account creation, authentication, password reset, and account deletion.
- `NoteServiceImpl` handles note ownership, CRUD operations, deletion, and synchronization.
- `AiService` uses Spring AI `ChatClient` with Google Gemini.
- `EmailService` renders the password-reset Thymeleaf template and sends it through Resend.

### DTO inventory

The `Dto` package contains the following request and response types:

- `AuthResponseDto`: JWT, user name, email, and operation message.
- `RegisterDto`: registration name, email, and password.
- `LoginRequestDto`: login email and password field named `pass`.
- `ForgotPassDto`: password-recovery email.
- `ResetPasswordDto`: recovery email, OTP, and replacement password.
- `NoteRequestDTO`: title, content, category, client timestamp, and vault flag.
- `NoteResponseDTO`: server note ID, note fields, update timestamp, and vault flag.
- `UpdateNoteRequest`: update request model retained in the DTO package.
- `NoteSyncDTO`: a note change with ID, content, timestamps, version, deletion marker, and vault flag.
- `NoteSyncRequestDTO`: client sync timestamp and list of changes.
- `NoteSyncResponseDTO`: server timestamp and list of returned changes.
- `AiSummarizeReqDto` and `AiSummarizeResDto`: summary input and output.
- `AiGenerateReqDto` and `AiGenerateResDto`: note-generation input and output.
- `AiKeyPointsReqDto` and `AiKeyPointsResDto`: key-point input and output.
- `ErrorResponseDto`: standardized error metadata and validation errors.
- `NotBlank`: a project-local annotation type currently present in the package.

### Exceptions and utilities

- `BadRequestException`, `ConflictException`, and `ResourceNotFoundException` represent domain-level API failures.
- `GlobalExceptionHandler` maps application, validation, security, database, and AI exceptions to JSON responses.
- `NotesMapper` converts persisted notes to note response and synchronization DTOs.

### Security components

- `WebSecurityConfg` disables CSRF for this stateless API, requires authentication for protected routes, and installs the JWT filter.
- `JwtAuthenticationFilter` reads `Authorization: Bearer <token>` and populates the Spring Security context.
- `AuthUtils` creates and validates tokens signed with the configured JWT secret.
- `CustomUserDetailsService` loads users by email.
- `CurrentUserService` retrieves the authenticated `User` from the security context.
- `SecurityErrorHandler` handles security-related authentication and authorization failures.

### Persistence

- `User` maps to the `app_users` table and implements Spring Security's `UserDetails` contract.
- `Note` maps to the `notes` table and belongs to one user.
- `UserRepository` provides user lookup and duplicate-email checks.
- `NotesRepository` provides user-scoped note lookup, synchronization queries, and bulk deletion.

## Requirements

- JDK 25.
- PostgreSQL database.
- Internet access for Resend email delivery and Google Gemini requests when those features are used.
- A configured Resend API key for password-reset email delivery.
- A configured Google Gemini API key for AI endpoints.
- Maven is optional because the repository includes Maven Wrapper scripts.

Verify the Java version:

```bash
java -version
```

The output must report Java 25 or a compatible runtime supported by the project configuration.

## Configuration

The application reads configuration from environment variables through `src/main/resources/application.properties`.

| Variable | Required | Description | Example |
| --- | --- | --- | --- |
| `DB_URL` | Yes | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/stealthpad` |
| `DB_USERNAME` | Yes | PostgreSQL username | `stealthpad` |
| `DB_PASSWORD` | Yes | PostgreSQL password | Set outside source control |
| `JWT_SECRET` | Yes | Secret used to sign and verify JWTs | Use a long random secret |
| `RESEND_API_KEY` | Yes for password reset | Resend API key | Set outside source control |
| `GEMINI_API_KEY` | Yes for AI endpoints | Google Gemini API key | Set outside source control |
| `PORT` | No | HTTP port; defaults to `8080` | `8080` |

Example local environment configuration for PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/stealthpad"
$env:DB_USERNAME = "stealthpad"
$env:DB_PASSWORD = "change-me"
$env:JWT_SECRET = "replace-with-a-long-random-secret"
$env:RESEND_API_KEY = "replace-with-resend-key"
$env:GEMINI_API_KEY = "replace-with-gemini-key"
$env:PORT = "8080"
```

Do not commit API keys, database passwords, JWT secrets, `.env` files, or other credentials. The repository ignores `.env` and `.env.*`, but deployment secrets must still be injected through the hosting platform or runtime environment.

## Running Locally

1. Create a PostgreSQL database:

```sql
CREATE DATABASE stealthpad;
```

2. Set the required environment variables.

3. Start the application from this directory.

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

The server listens on `http://localhost:8080` unless `PORT` is set.

The API base URL is:

```text
http://localhost:8080
```

## Building and Testing

Run the test suite:

Windows:

```powershell
.\mvnw.cmd test
```

Linux or macOS:

```bash
./mvnw test
```

Build the executable JAR:

```bash
./mvnw clean package
```

On Windows, use the Maven Wrapper command shown below:

```powershell
.\mvnw.cmd clean package
```

The packaged application is written to `target/stealthpad-backend-0.0.1-SNAPSHOT.jar`.

Run the packaged JAR:

```bash
java -jar target/stealthpad-backend-0.0.1-SNAPSHOT.jar
```

The current test suite includes a Spring application context smoke test in `StealthpadBackendApplicationTests`.

## Running with Docker

The Dockerfile uses `eclipse-temurin:25-jdk`, copies the backend into `/app`, builds the application with Maven Wrapper, exposes port `8080`, and starts the generated JAR.

Build the image:

```bash
docker build -t stealthpad-backend .
```

Run the container with environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/stealthpad" \
  -e DB_USERNAME="stealthpad" \
  -e DB_PASSWORD="change-me" \
  -e JWT_SECRET="replace-with-a-long-random-secret" \
  -e RESEND_API_KEY="replace-with-resend-key" \
  -e GEMINI_API_KEY="replace-with-gemini-key" \
  stealthpad-backend
```

On PowerShell, use backticks for line continuation or provide the options on one line.

The image builds with `-DskipTests`, so run `mvnw test` separately in CI before building a production image.

## Authentication

Registration and login return a JWT in the `jwt` property of `AuthResponseDto`.

Send the token on protected requests:

```http
Authorization: Bearer <jwt>
```

Tokens:

- Use the user's email as the JWT subject.
- Are signed using `JWT_SECRET`.
- Are valid for 30 days.
- Are checked against the current user in the database.
- Are not stored in server-side sessions.

The `/api/auth/**` routes are public. Every other route requires authentication, including `/api/health`.

## API Reference

All request and response bodies use JSON unless noted otherwise. Protected endpoints require the `Authorization` header.

### Health

#### `GET /api/health`

Returns a service status object. Because the security configuration protects every route outside `/api/auth/**`, this endpoint requires a valid JWT.

Example response:

```json
{
  "status": "UP",
  "service": "StealthPad Backend",
  "timestamp": "2026-09-16T12:00:00Z"
}
```

### Authentication

#### `POST /api/auth/register`

Creates an account, hashes the password with BCrypt, stores the user, and returns a JWT.

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "password": "a-secure-password"
}
```

Response fields:

- `jwt`: signed access token.
- `name`: account name.
- `email`: account email.
- `message`: operation result.

#### `POST /api/auth/login`

Authenticates a user by email and password and returns a JWT.

Request:

```json
{
  "email": "ada@example.com",
  "pass": "a-secure-password"
}
```

#### `POST /api/auth/forgot-password`

Starts password recovery. If the email belongs to an account, the backend generates a six-digit OTP, stores it for 10 minutes, and sends it using Resend. The response is intentionally generic whether or not the account exists.

Request:

```json
{
  "email": "ada@example.com"
}
```

Successful response:

```text
If an account exists with this email, an OTP has been sent.
```

#### `POST /api/auth/reset-password`

Validates the email, OTP, and expiry time, then replaces the password with a BCrypt hash. A successful reset invalidates the OTP.

Request:

```json
{
  "email": "ada@example.com",
  "otp": "123456",
  "newPassword": "another-secure-password"
}
```

Successful response:

```text
Password reset successful
```

### Notes

All note operations are scoped to the authenticated user. A note ID belonging to another user is not accessible through these endpoints.

#### `POST /api/notes`

Creates a note.

Request fields:

```json
{
  "title": "Project ideas",
  "content": "Build a private note workflow.",
  "category": "Ideas",
  "timestamp": 1758000000000,
  "vault": false
}
```

The server initializes `version` to `1`, sets `updatedAt` using server time, and associates the note with the authenticated user.

Response fields include `id`, `title`, `content`, `category`, `updatedAt`, and `vault`.

#### `GET /api/notes`

Returns all non-deleted notes owned by the authenticated user.

#### `PUT /api/notes/{id}`

Updates a note owned by the authenticated user.

The request uses the same fields as note creation. The server updates `updatedAt` and increments the note version through the JPA lifecycle callback.

#### `DELETE /api/notes/{id}`

Permanently deletes one note owned by the authenticated user. Returns HTTP `204 No Content` on success.

#### `DELETE /api/notes`

Permanently deletes all notes owned by the authenticated user.

Successful response:

```text
All notes deleted.
```

#### `POST /api/notes/sync`

Uploads client changes and returns server changes. See [Note Synchronization](#note-synchronization) for the conflict rules and payload structure.

Request:

```json
{
  "lastSyncAt": 1758000000000,
  "changes": [
    {
      "id": 42,
      "title": "Updated title",
      "content": "Updated content",
      "category": "Work",
      "timestamp": 1758000000000,
      "updatedAt": 1758000000000,
      "version": 3,
      "deleted": false,
      "vault": true
    }
  ]
}
```

Response:

```json
{
  "serverTime": 1758000001000,
  "changes": []
}
```

### User account

#### `DELETE /api/user/account`

Deletes the authenticated user's notes and then deletes the user account in one transactional service operation.

Successful response:

```text
Account deleted successfully
```

### AI operations

All AI endpoints require authentication and accept a `text` property. The text must be non-blank and must not exceed 10,000 characters.

#### `POST /api/ai/summarize`

Generates a concise summary, normally two to four sentences.

Request:

```json
{
  "text": "Long note content to summarize"
}
```

Response:

```json
{
  "summary": "Generated summary",
  "generatedAt": "2026-09-16T12:00:00"
}
```

#### `POST /api/ai/generate`

Turns an idea, rough text, question, or instruction into a structured note.

Response fields are `generatedNote` and `generatedAt`.

#### `POST /api/ai/key-points`

Extracts three to seven key points from the supplied text.

Response:

```json
{
  "keyPoints": ["First important point", "Second important point"],
  "generatedAt": "2026-09-16T12:00:00"
}
```

## Data Model

### `app_users`

The `User` entity stores:

- `id`: generated numeric primary key.
- `name`: display name.
- `mail`: unique email column used as the login identifier.
- `pass`: BCrypt password hash.
- `reset_otp`: temporary password-reset OTP.
- `reset_otp_expiry`: OTP expiration timestamp.
- `notes`: one-to-many relationship with the user's notes.

### `notes`

The `Note` entity stores:

- `id`: generated numeric primary key.
- `title`: required text title.
- `content`: required text content.
- `category`: optional category.
- `timestamp`: client note timestamp.
- `updatedAt`: server-managed update timestamp in epoch milliseconds.
- `version`: server-managed version number.
- `deleted`: deletion marker used by synchronization.
- `is_vault`: whether the note is marked as a vault note.
- `user_id`: owning user relationship.

New notes start with version `1` and `deleted = false`. Updates set `updatedAt` to the current server time and increment `version`.

## Note Synchronization

The `/api/notes/sync` operation supports bidirectional incremental synchronization.

1. The client sends `lastSyncAt` and an optional list of local `changes`.
2. A change without an `id` is created as a new server note.
3. An existing change is processed only when the note belongs to the authenticated user.
4. If the client version is greater than or equal to the server version, the client fields are accepted and the server increments the version.
5. If the client version is older than the server version, the server version is returned and the client change is not applied.
6. The backend then queries notes updated after `lastSyncAt`.
7. Duplicate notes are removed from the response.
8. The response includes `serverTime` and the combined changes.

Deletion is represented in synchronization using the `deleted` flag. The ordinary `DELETE` endpoints physically remove records, while synchronization can preserve a deletion marker for clients to receive.

## Error Responses

Errors are returned by `GlobalExceptionHandler` in a consistent `ErrorResponseDto` shape:

```json
{
  "timestamp": "2026-09-16T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Malformed JSON request",
  "path": "/api/notes",
  "errors": {
    "text": "Text cannot be empty"
  }
}
```

The `errors` property is omitted when there are no field-level validation errors.

Common status codes:

| Status | Meaning |
| --- | --- |
| `400` | Malformed input, invalid request parameters, invalid OTP, expired OTP, or validation failure |
| `401` | Missing or invalid authentication |
| `403` | Authenticated user does not have permission |
| `404` | Requested resource does not exist |
| `409` | Duplicate account or database integrity conflict |
| `502` | Non-transient AI provider failure |
| `503` | Temporary AI provider failure |
| `500` | Unexpected server error |

## Security Behavior

- Authentication is stateless; no HTTP session is used.
- CSRF protection is disabled because the API uses bearer-token authentication rather than cookie sessions.
- `/api/auth/**` is public.
- All other routes require authentication.
- JWTs are checked for signature, subject, expiry, and matching user identity.
- Passwords are never stored in plaintext; `BCryptPasswordEncoder` hashes them before persistence.
- Note queries include both note ID and authenticated user, preventing cross-account note access through guessed IDs.
- Password recovery returns the same initial message for existing and unknown email addresses to reduce account enumeration.
- Secrets are supplied through environment variables and must not be committed.
- AI requests send supplied note text to the configured Google Gemini provider. Review data handling and provider terms before using this feature with sensitive content.

## Email and Password Recovery

`EmailService` renders `src/main/resources/templates/forgot-password.html` with the generated `otp` variable and sends the resulting HTML email through Resend.

The configured sender is:

```text
StealthPad <noreply@stealthpad.kavyadev.in>
```

Password-reset OTPs:

- Are six digits long.
- Expire after 10 minutes.
- Are stored temporarily with the user record.
- Are cleared after a successful reset.
- Are delivered only when the requested email belongs to an account.

A missing or invalid Resend configuration causes email delivery to fail and is surfaced as an application error.

## AI Features

`AiService` creates a Spring AI `ChatClient` backed by the configured Google Gemini model:

```text
 gemini-2.5-flash
```

The service provides three operations:

- Summarization: returns two to four concise sentences and preserves information from the input.
- Note generation: expands rough ideas or instructions into structured note content.
- Key-point extraction: returns three to seven concise points.

Input validation limits all three operations to 10,000 characters. AI provider failures are translated into `503 Service Unavailable` or `502 Bad Gateway` responses by the global exception handler.

## Database Behavior

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This allows Hibernate to create or update tables during development. It is convenient for local setup but should be replaced with a controlled migration strategy for production. Back up the database before changing entity mappings or deploying a new version.

SQL logging is disabled by default:

```properties
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

## Troubleshooting

### Application fails to start because of datasource configuration

Check that PostgreSQL is running and that `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are set in the same process environment used to start the application.

### Requests return `401 Unauthorized`

Confirm that the request includes a current token in this exact format:

```http
Authorization: Bearer <jwt>
```

Also verify that `JWT_SECRET` has not changed since the token was issued.

### Password reset email is not sent

Verify `RESEND_API_KEY`, the sender domain configuration in Resend, and the application logs. The email template is located at `src/main/resources/templates/forgot-password.html`.

### AI requests fail

Verify `GEMINI_API_KEY`, network access, provider availability, and the configured Gemini model. Inputs must be non-blank and no longer than 10,000 characters.

### Docker cannot connect to local PostgreSQL

When PostgreSQL runs on the host machine, use `host.docker.internal` rather than `localhost` in `DB_URL` on Docker Desktop.

## Development Notes

- Keep API credentials and database credentials outside source control.
- Use the Maven Wrapper so local and CI builds use the repository's Maven configuration.
- Run the test suite before packaging or publishing a container image.
- Add database migrations before relying on schema changes in production.
- Add endpoint-level and synchronization conflict tests as the API evolves.
- Keep the Android client's request field names aligned with the DTOs documented here, especially the login field `pass` and note field `vault`.
- The generated `target/` directory contains build artifacts and is ignored by Git.
