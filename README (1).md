# TaskFlow API

Une API de gestion de tâches collaboratives inspirée par Trello/Jira, construite avec Spring Boot et JWT authentication.

## Table des matières

- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Exemples d'utilisation](#exemples-dutilisation)
- [Sécurité](#sécurité)
- [Stack Technique](#stack-technique)

---

## Features

### Workspace Management
- Créer/modifier/supprimer des workspaces (équipes)
- Gérer les membres avec rôles (ADMIN, MEMBER, VIEWER)
- Invite et gestion d'accès granulaires

### Project Management
- Créer/modifier/supprimer des projets
- Hiérarchie: Workspace → Project → Task
- Permissions au niveau du projet

### Task Management
- CRUD complet pour les tâches
- Statuts: PENDING, IN_PROGRESS, COMPLETED
- Priorités: LOW, MEDIUM, HIGH, URGENT
- Assignation d'utilisateurs
- Dates de début et limite
- Sous-tâches avec statuts indépendants
- Auto-update du statut parent basé sur les sous-tâches

### Collaboration
- Commentaires sur chaque tâche
- Modifier/supprimer ses propres commentaires
- Activity Log complet (historique de tous les changements)
- Tracking des actions: création, modification, statut, assignation

### Search & Filter
- Recherche par texte (titre + description)
- Filtrer par statut, priorité, dates
- Filtrer par assigné
- Recherche avancée combinée
- Pagination complète

### Sécurité
- JWT Authentication (Access + Refresh tokens)
- Token Rotation automatique
- Permissions granulaires par projet/workspace
- Audit trail complet

---

## Architecture

```
Workspace (Équipe)
    ├─ WorkspaceMember (ADMIN, MEMBER, VIEWER)
    └─ Project (Board)
        ├─ ProjectMember (ADMIN, MEMBER, VIEWER)
        ├─ Task
        │   ├─ Comment
        │   ├─ SubTask
        │   └─ ActivityLog
        └─ ...
```

### Entités Principales

| Entity | Description |
|--------|-------------|
| **User** | Utilisateur du système |
| **Workspace** | Équipe/Organisation |
| **WorkspaceMember** | Appartenance + rôle dans workspace |
| **Project** | Projet/Board |
| **ProjectMember** | Appartenance + rôle dans project |
| **Task** | Tâche individuelle |
| **SubTask** | Sous-tâche (décomposition d'une task) |
| **Comment** | Commentaire sur une tâche |
| **ActivityLog** | Historique des changements |

### Rôles et Permissions

#### Workspace Roles
- **ADMIN**: Tout faire, gérer les membres, supprimer le workspace
- **MEMBER**: Créer/modifier ses propres projets
- **VIEWER**: Consultation uniquement

#### Project Roles
- **ADMIN**: Modifier le projet, gérer les membres, modifier/supprimer les tâches
- **MEMBER**: Créer/modifier/supprimer les tâches, changer les statuts
- **VIEWER**: Consultation uniquement

---

## Installation

### Prérequis
- Java 17+ (ou JDK 25)
- MySQL 8.0+
- Maven 3.6+

### 1. Cloner le projet
```bash
git clone https://github.com/ton-repo/taskflow.git
cd taskflow
```

### 2. Configurer la base de données

Créer une base de données MySQL:
```sql
CREATE DATABASE taskflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Ajouter les rôles initiaux
INSERT INTO role (name) VALUES ('ROLE_USER');
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
```

### 3. Configurer application.properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/taskflow
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false

# Server
server.port=8080
spring.application.name=taskflow

# JWT Configuration
app.jwt.secret=votre_secret_base64_ici
app.jwt.expiration=900000
app.jwt.refreshExpiration=604800000
app.jwt.issuer=taskflow-app
```

**Générer un JWT secret:**
```bash
openssl rand -base64 32
```

### 4. Compiler et lancer
```bash
mvn clean install
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`

---

## Configuration

### JWT Tokens

Access Token:
- Durée: 15 minutes (900000ms)
- Claims: sub, iss, iat, exp, role, type

Refresh Token:
- Durée: 7 jours (604800000ms)
- Claims: sub, iss, iat, exp, type

Headers requis:
```
Authorization: Bearer <access_token>
```

### Response Format

Toutes les réponses sont en JSON:

Success (2xx):
```json
{
  "id": 1,
  "name": "Mon workspace",
  "createdBy": "user@example.com",
  "createdAt": "2026-03-04T14:21:44.893516",
  "updatedAt": null
}
```

Error (4xx/5xx):
```json
{
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-03-04T14:23:50.568421",
  "status": 400
}
```

---

## API Documentation

### Authentication

#### Register
```
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword123!"
}
```

#### Refresh Token
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

---

### Workspace Endpoints

#### Create Workspace
```
POST /api/workspaces
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Mon équipe",
  "description": "Description du workspace"
}
```

#### List My Workspaces
```
GET /api/workspaces?page=0&size=20
Authorization: Bearer <token>
```

#### Get Workspace
```
GET /api/workspaces/{id}
Authorization: Bearer <token>
```

#### Update Workspace
```
PUT /api/workspaces/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Nouveau nom",
  "description": "Nouvelle description"
}
```

#### Delete Workspace
```
DELETE /api/workspaces/{id}
Authorization: Bearer <token>
```

#### Add Member to Workspace
```
POST /api/workspaces/{id}/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "newmember@example.com",
  "role": "MEMBER"
}
```

#### Remove Member from Workspace
```
DELETE /api/workspaces/{id}/members/{memberEmail}
Authorization: Bearer <token>
```

---

### Project Endpoints

#### Create Project
```
POST /api/workspaces/{workspaceId}/projects
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Projet TaskFlow",
  "description": "Premier projet"
}
```

#### List Projects in Workspace
```
GET /api/workspaces/{workspaceId}/projects?page=0&size=20
Authorization: Bearer <token>
```

#### Get Project
```
GET /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <token>
```

#### Update Project
```
PUT /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Nouveau nom",
  "description": "Nouvelle description"
}
```

#### Delete Project
```
DELETE /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <token>
```

#### Add Member to Project
```
POST /api/workspaces/{workspaceId}/projects/{projectId}/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "user@example.com",
  "role": "MEMBER"
}
```

---

### Task Endpoints

#### Create Task
```
POST /api/workspaces/{wId}/projects/{pId}/tasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Implémenter feature X",
  "description": "Description détaillée",
  "status": "PENDING",
  "priority": "HIGH",
  "startDate": "2026-03-04",
  "dueDate": "2026-03-10"
}
```

#### List Tasks
```
GET /api/workspaces/{wId}/projects/{pId}/tasks?page=0&size=20
Authorization: Bearer <token>
```

#### Get Task
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{taskId}
Authorization: Bearer <token>
```

#### Update Task
```
PUT /api/workspaces/{wId}/projects/{pId}/tasks/{taskId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Nouveau titre",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

#### Delete Task
```
DELETE /api/workspaces/{wId}/projects/{pId}/tasks/{taskId}
Authorization: Bearer <token>
```

#### Filter by Status
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/status?status=PENDING
Authorization: Bearer <token>
```

---

### SubTask Endpoints

#### Create SubTask
```
POST /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/subtasks
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Étape 1: Créer la base",
  "description": "Initialiser le schema"
}
```

#### List SubTasks
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/subtasks?page=0&size=20
Authorization: Bearer <token>
```

#### Get SubTask
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/subtasks/{subTaskId}
Authorization: Bearer <token>
```

#### Update SubTask
```
PUT /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/subtasks/{subTaskId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Titre modifié",
  "status": "IN_PROGRESS"
}
```

#### Delete SubTask
```
DELETE /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/subtasks/{subTaskId}
Authorization: Bearer <token>
```

Comportement special:
- Quand TOUTES les sous-tâches sont COMPLETED, la tâche parente devient automatiquement COMPLETED
- Quand au moins une sous-tâche est IN_PROGRESS, la tâche parente devient IN_PROGRESS
- Quand il n'y a pas de sous-tâches, la tâche parente reste à son statut manuel

---

#### Add Comment
```
POST /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/comments
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Ceci est un commentaire"
}
```

#### List Comments
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/comments?page=0&size=20
Authorization: Bearer <token>
```

#### Update Comment
```
PUT /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/comments/{commentId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Contenu modifié"
}
```

#### Delete Comment
```
DELETE /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/comments/{commentId}
Authorization: Bearer <token>
```

---

### Activity Log Endpoints

#### Get Activity History
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/activity?page=0&size=20
Authorization: Bearer <token>
```

#### Filter Activity by Type
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/{tId}/activity/filter?type=STATUS_CHANGED
Authorization: Bearer <token>
```

**Types disponibles:**
- TASK_CREATED
- TASK_UPDATED
- STATUS_CHANGED
- PRIORITY_CHANGED
- ASSIGNED
- UNASSIGNED
- COMMENT_ADDED
- COMMENT_DELETED
- DUE_DATE_CHANGED
- DESCRIPTION_CHANGED
- TASK_DELETED

---

### Search & Filter Endpoints

#### Search by Text
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/search?q=bug&page=0&size=20
Authorization: Bearer <token>
```

#### Filter by Status
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/status?status=PENDING
Authorization: Bearer <token>
```

#### Filter by Priority
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/priority?priority=HIGH
Authorization: Bearer <token>
```

#### Filter by Due Date
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/due-date/before?before=2026-03-10
Authorization: Bearer <token>
```

#### Filter by Assigned
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/filter/assigned?to=user@example.com
Authorization: Bearer <token>
```

#### Advanced Search
```
GET /api/workspaces/{wId}/projects/{pId}/tasks/search/advanced?q=bug&status=PENDING&priority=HIGH
Authorization: Bearer <token>
```

---

## Exemples d'utilisation

### Workflow complet

#### 1. Register et Login
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice",
    "email": "alice@example.com",
    "password": "SecurePass123!"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123!"
  }'

# Récupère le access_token
TOKEN="eyJhbGciOiJIUzI1NiJ9..."
```

#### 2. Créer un Workspace
```bash
curl -X POST http://localhost:8080/api/workspaces \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mon équipe",
    "description": "Équipe de développement"
  }'

# Récupère workspace_id: 1
```

#### 3. Créer un Projet
```bash
curl -X POST http://localhost:8080/api/workspaces/1/projects \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TaskFlow API",
    "description": "Implémentation de l'"'"'API REST"'"'"'"
  }'

# Récupère project_id: 1
```

#### 4. Créer une Tâche
```bash
curl -X POST http://localhost:8080/api/workspaces/1/projects/1/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Implémenter Comments",
    "description": "Ajouter la fonctionnalité de commentaires",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "startDate": "2026-03-04",
    "dueDate": "2026-03-10"
  }'

# Récupère task_id: 1
```

#### 5. Ajouter un Commentaire
```bash
curl -X POST http://localhost:8080/api/workspaces/1/projects/1/tasks/1/comments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "C'"'"'est commencé!"
  }'
```

#### 6. Rechercher les tâches
```bash
# Recherche par texte
curl "http://localhost:8080/api/workspaces/1/projects/1/tasks/search?q=Comments" \
  -H "Authorization: Bearer $TOKEN"

# Filtrer par statut
curl "http://localhost:8080/api/workspaces/1/projects/1/tasks/filter/status?status=IN_PROGRESS" \
  -H "Authorization: Bearer $TOKEN"

# Recherche avancée
curl "http://localhost:8080/api/workspaces/1/projects/1/tasks/search/advanced?q=feature&status=PENDING&priority=HIGH" \
  -H "Authorization: Bearer $TOKEN"
```

#### 7. Consulter l'Activity Log
```bash
curl "http://localhost:8080/api/workspaces/1/projects/1/tasks/1/activity" \
  -H "Authorization: Bearer $TOKEN"
```

#### 8. Créer des sous-tâches
```bash
# Créer une sous-tâche
curl -X POST http://localhost:8080/api/workspaces/1/projects/1/tasks/1/subtasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Étape 1: Initialiser la base",
    "description": "Créer le schema"
  }'

# Lister les sous-tâches
curl "http://localhost:8080/api/workspaces/1/projects/1/tasks/1/subtasks" \
  -H "Authorization: Bearer $TOKEN"

# Mettre à jour une sous-tâche
curl -X PUT http://localhost:8080/api/workspaces/1/projects/1/tasks/1/subtasks/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Étape 1: DONE",
    "status": "COMPLETED"
  }'
```

Nota: Quand toutes les sous-tâches sont COMPLETED, la tâche parente devient automatiquement COMPLETED.


---

## Sécurité

### Authentification
- JWT avec HS256
- Access tokens: 15 minutes
- Refresh tokens: 7 jours
- Token rotation automatique

### Autorisation
- Permissions granulaires par Workspace et Project
- Vérification d'accès à chaque niveau
- Seul le créateur peut modifier/supprimer ses commentaires
- Audit trail complet

### Best Practices
- Utiliser HTTPS en production
- Ne jamais exposer le JWT secret
- Valider tous les inputs
- Rate limiting recommandé

---

## Stack Technique

| Composant | Version |
|-----------|---------|
| Spring Boot | 4.0.2 |
| Spring Data JPA | 4.0.2 |
| Spring Security | 7.0.2 |
| MySQL | 8.0+ |
| JWT (JJWT) | 0.11.5 |
| Lombok | 1.18.42 |
| Maven | 3.6+ |
| Java | 17+ |

Dépendances clés:
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- mysql-connector-java
- jjwt (JWT)
- lombok

---

## License

MIT License

---

## Support

Pour toute question ou bug report, créer une issue sur GitHub.

---

## Changelog

### v1.0.0 (2026-03-04)
- Architecture Workspace → Project → Task
- JWT Authentication avec Refresh tokens
- Système de rôles granulaires
- Comments sur les tâches
- Activity Log complet
- Search & Filter avancé
- Gestion des permissions
- Pagination complète

---

Made with love by TaskFlow Team
