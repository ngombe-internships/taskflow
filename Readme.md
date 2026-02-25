TaskFlow API

TaskFlow is a secure RESTful API for task management built with Spring Boot and secured using Spring Security with JWT authentication.

The application provides user registration, authentication, role-based authorization, and will support full task management features.

 Features

 JWT Authentication

 User Registration & Login

 Role-Based Authorization (USER / ADMIN)

 Stateless Security (No Sessions)

 Password Encryption using BCrypt

 MySQL Database Integration

 Layered Architecture (Controller → Service → Repository)

🛠 Technologies Used

Java 17

Spring Boot

Spring Security

MySQL

JWT (io.jsonwebtoken)

Lombok

Maven

 Project Structure
com.duva.taskflow
│
├── config
│   ├── SecurityConfig
│   └── DataInitializer
│
├── controller
│   └── AuthController
│
├── dto
│   ├── LoginRequest
│   └── RegisterRequest
│
├── entity
│   ├── User
│   └── Role
│
├── repository
│   ├── UserRepository
│   └── RoleRepository
│
├── security
│   ├── JwtService
│   ├── JwtAuthenticationFilter
│   └── CustomUserDetailsService
│
├── service
│   └── AuthService
│
└── TaskflowApplication
 Authentication Flow

User registers via:

POST /api/auth/register

User logs in via:

POST /api/auth/login

API returns a JWT token.

Client must include the token in future requests:

Authorization: Bearer <token>

JWT filter validates the token and grants access.

📡 API Endpoints
 Public Endpoints
Method	Endpoint	Description
POST	/api/auth/register	Register new user
POST	/api/auth/login	Authenticate user
 Protected Endpoints
Endpoint	Access
/api/admin/**	ROLE_ADMIN
Other endpoints	Authenticated users
 Database Schema
users
Field	Type
id	BIGINT
name	VARCHAR
email	VARCHAR (unique)
password	VARCHAR
enabled	BOOLEAN
role_id	FK
roles
Field	Type
id	BIGINT
name	VARCHAR (unique)
 Configuration

Make sure MySQL is running and create a database:

CREATE DATABASE taskflow_db;

Configure your application.yml or application.properties:

spring:
datasource:
url: jdbc:mysql://localhost:3306/taskflow_db
username: root
password: root
▶ Running the Project
mvn clean install
mvn spring-boot:run

The application will start at:

http://localhost:8080
 Testing with Postman

Register a user

Login and copy the token

Use Authorization: Bearer <token> for protected routes

 Upcoming Features

Task CRUD

Subtask management

Deadline tracking

Filtering by status and priority

Dashboard statistics