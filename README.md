# Conclave

Conclave is a web application for generating multimedia content through external artificial intelligence providers. The project consists of an Angular frontend and a Spring Boot backend.

## Table of Contents

- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Features](#features)
- [AI Providers](#ai-providers)
- [Security and Roles](#security-and-roles)
- [REST API](#rest-api)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Running the Project](#running-the-project)
- [API Documentation](#api-documentation)

## Technologies

### Frontend

- Angular 22
- TypeScript
- RxJS
- Bootstrap 5.3.3
- Animate.css

### Backend

- Java 21
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring Mail
- Springdoc OpenAPI 2.8.9
- ModelMapper
- Gson
- JJWT 0.12.6
- MySQL Connector/J

## Project Structure

The repository contains two independent applications:

```text
.
├── conclaveBack/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/co/edu/unbosque/conclave/
│   │   │   │   ├── configuration/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   ├── model/
│   │   │   │   ├── repository/
│   │   │   │   ├── security/
│   │   │   │   ├── service/
│   │   │   │   └── util/
│   │   │   └── resources/
│   │   └── test/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
│
└── front/
    ├── public/
    ├── src/
    │   └── app/
    │       ├── component/
    │       └── services/
    ├── angular.json
    └── package.json
```

### Backend

The backend follows a layered structure:

- `configuration`: application configuration, database initialization, asynchronous execution, and OpenAPI setup.
- `controller`: REST endpoints.
- `dto`: objects used to receive and return data.
- `exception`: custom exceptions and centralized error handling.
- `model`: application entities and enumerations.
- `repository`: data access interfaces.
- `security`: Spring Security configuration and JWT authentication.
- `service`: business logic, email authentication, API key management, and communication with AI providers.
- `util`: utility classes, including AES encryption.

### Frontend

The Angular application is organized around UI components and services responsible for communication with the backend, authentication, and generation workflows.

Its components cover the home page, models, login, registration, account verification, password recovery, image generation, video generation, sound generation, profile, and administration.

## Features

Conclave includes the following features:

- User registration.
- User login.
- Email-based account verification.
- Verification code resending.
- Password reset requests and confirmation.
- Authenticated user profile retrieval.
- AI image generation.
- AI video generation.
- AI audio generation.
- Personal prompt history.
- Administrative prompt history.
- Provider API key management.
- Administrative user management.
- Centralized API error handling.

Content generation is requested through the `POST /prompt/generar` endpoint.

## AI Providers

The backend defines the following providers through the `AIProvider` enumeration:

| Provider | Supported generation types |
|---|---|
| OpenRouter | Images |
| Google Gemini | Images and sound |
| Cloudflare Workers AI | Images and sound |
| Hugging Face – MiniMax H3 FlashGen | Video |
| Hugging Face – MiniMax H3 Turbo | Video |
| Hugging Face – Omni Video | Video |
| Hugging Face – ModelScope Text-to-Video | Video |
| Pollinations.ai | Images |
| Together AI | Images |
| ElevenLabs | Sound |

Provider availability depends on the requested content type and the corresponding provider configuration. Provider API keys can be managed through the administrative endpoints.

## Security and Roles

The backend uses Spring Security and JSON Web Tokens (JWT) for authentication.

- The API uses stateless session management.
- Passwords are encoded using BCrypt.
- Authentication endpoints under `/auth/**` are publicly accessible.
- Swagger and OpenAPI documentation routes are publicly accessible.
- `/user/me`, `/prompt/**`, and `/history/me` are accessible to users with the `USER` or `ADMIN` role.
- Administrative user and history routes require the `ADMIN` role, subject to the specific access rules configured for `/user/me` and `/history/me`.
- Other routes require authentication.

The frontend stores the session token in `localStorage` using the `conclave_token` key and sends it through the `Authorization` header with the `Bearer` scheme.

## REST API

The following endpoints are declared by the backend controllers.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/login` | Authenticate a user. |
| `POST` | `/auth/register` | Register a new account. |
| `POST` | `/auth/reenviar-codigo` | Resend the account verification code. |
| `POST` | `/auth/verificar-registro` | Verify an account registration. |
| `POST` | `/auth/solicitar-reset` | Request a password reset. |
| `POST` | `/auth/confirmar-reset` | Confirm a password reset. |
| `GET` | `/user/me` | Retrieve the authenticated user's profile. |
| `POST` | `/user/createjson` | Create a user from JSON data. |
| `GET` | `/user/getall` | Retrieve all users. |
| `GET` | `/user/count` | Retrieve the number of users. |
| `GET` | `/user/exists/{id}` | Check whether a user exists. |
| `GET` | `/user/getbyid/{id}` | Retrieve a user by ID. |
| `PUT` | `/user/updatejson` | Update a user from JSON data. |
| `PUT` | `/user/setenabled/{id}` | Change a user's enabled status. |
| `DELETE` | `/user/deletebyid/{id}` | Delete a user by ID. |
| `POST` | `/prompt/generar` | Request content generation. |
| `GET` | `/history/me` | Retrieve the authenticated user's prompt history. |
| `GET` | `/history` | Retrieve the complete prompt history. |
| `GET` | `/history/user/{id}` | Retrieve a user's prompt history by user ID. |
| `GET` | `/admin/apikey/getall` | Retrieve registered provider API keys in masked form. |
| `PUT` | `/admin/apikey/update/{provider}` | Update a provider API key and its enabled status. |
| `PUT` | `/admin/apikey/setenabled/{provider}` | Change a provider's enabled status. |

Administrative endpoints are protected by the `ADMIN` role according to the backend security configuration.

## Requirements

- Java 21.
- Node.js and npm compatible with the Angular version used by the project.
- A MySQL database.
- Email account settings for account verification and password recovery.
- API credentials for the external providers you intend to use.

The backend includes Maven Wrapper scripts (`mvnw` and `mvnw.cmd`), so a global Maven installation is not required.

## Configuration

### Backend

Backend configuration is managed through:

```text
conclaveBack/src/main/resources/application.properties
```

Set the environment-specific database, email, and security properties required by the application.

The email service uses the `app.mail.from` property to define the sender address. JWT signing uses the `jwt.secret` property.

Provider API keys are managed through the application's administrative functionality. Do not commit private credentials or secrets to the repository.

### Frontend

The Angular API service defines the backend base URL in:

```text
front/src/app/services/apiservice.ts
```

The configured base URL is:

```text
https://gpcueb.org/conclave-0.0.1-SNAPSHOT
```

The backend CORS configuration includes the following origins:

- `http://localhost:8080`
- `http://localhost:8081`
- `http://localhost:5173`
- `http://localhost:4200`
- `https://conclaveia.netlify.app`

If the frontend is served from a different origin, that origin can be added to the backend CORS configuration.

## Running the Project

Run the backend and frontend in separate terminals.

### 1. Backend

Open a terminal in the backend directory:

```bash
cd conclaveBack
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS:

```bash
./mvnw spring-boot:run
```

To package the backend:

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

### 2. Frontend

Open another terminal in the frontend directory:

```bash
cd front
```

Install the dependencies:

```bash
npm install
```

Start the Angular development server:

```bash
npm start
```

The `start` script runs `ng serve`.

To create a production build:

```bash
npm run build
```

To run the configured tests:

```bash
npm test
```

## API Documentation

The backend uses Springdoc OpenAPI with a custom documentation configuration.

When the backend is running, the Swagger UI and OpenAPI document are available at:

```text
/swagger-ui/index.html
/v3/api-docs
```

The backend security configuration allows public access to the Swagger and OpenAPI routes.
