 PRÉSENTATION DU PROJET

TaskFlow est une API REST sécurisée développée avec Spring Boot permettant la gestion 
des Tâches (Tasks) et Sous-Tâches (SubTasks) avec une logique métier intelligente 
et une authentification basée sur JWT.

Fonctionnalités principales :
- Authentification JWT
- Autorisation basée sur les rôles (ROLE_USER / ROLE_ADMIN)
- Pagination des données
- Recalcul automatique du statut des tâches
- Architecture en couches propre (Clean Architecture)
- Base de données MySQL
- Java 17
- Maven


 ARCHITECTURE

Le projet suit une architecture en couches :

Controller → Service → Repository → Base de données

Structure des packages :

com.duva.taskflow
│
├── config        → Configuration sécurité et initialisation
├── controller    → Contrôleurs REST
├── dto           → Objets de transfert de données (DTO)
├── entity        → Entités JPA
├── repository    → Interfaces Spring Data JPA
├── service       → Logique métier
└── security      → Gestion JWT et authentification

 SÉCURITÉ

L’authentification repose sur :
- Spring Security
- JWT (JSON Web Token)
- Contrôle d’accès basé sur les rôles

Endpoints publics :
POST /api/auth/register
POST /api/auth/login

Tous les autres endpoints nécessitent un token JWT valide.


 LOGIQUE MÉTIER INTELLIGENTE

Recalcul automatique du statut d’une Task :

Lorsqu’une SubTask est mise à jour :

- Si toutes les SubTasks sont COMPLETED → la Task devient COMPLETED
- Si au moins une SubTask n’est pas complétée → la Task devient IN_PROGRESS
- S’il n’y a aucune SubTask → la Task devient PENDING

Cette logique garantit la cohérence des données et un comportement intelligent du système.


 BASE DE DONNÉES

- MySQL
- JPA / Hibernate
- Les enums sont stockés en STRING
- Mise à jour automatique du schéma (ddl-auto=update)


 GUIDE D’INSTALLATION

1. Cloner le projet :
   git clone <url_du_repository>

2. Configurer la base de données dans application.properties :

   spring.datasource.url=jdbc:mysql://localhost:3306/taskflow
   spring.datasource.username=root
   spring.datasource.password=motdepasse
   spring.jpa.hibernate.ddl-auto=update

3. Lancer l’application :
   mvn clean install
   mvn spring-boot:run

Serveur accessible sur :
http://localhost:8080



 ENDPOINTS API

Authentification :
POST /api/auth/register
POST /api/auth/login

Tasks :
POST   /api/tasks
GET    /api/tasks?page=0&size=10
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
DELETE /api/tasks/{id}

SubTasks :
POST   /api/subtasks/task/{taskId}
GET    /api/subtasks/task/{taskId}?page=0&size=10
PUT    /api/subtasks/{subTaskId}
DELETE /api/subtasks/{subTaskId}


 PAGINATION

Les endpoints paginés supportent :
?page=0&size=10&sort=createdAt,desc


 TESTS

Utiliser Postman :

1. Se connecter et récupérer le JWT
2. Ajouter dans les headers :
   Authorization: Bearer <token>
3. Tester les endpoints sécurisés


 TECHNOLOGIES UTILISÉES

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- Spring Data JPA
- MySQL
- Maven
- Lombok


 AMÉLIORATIONS FUTURES

- Système de refresh token
- Gestion des commentaires sur les tâches
- Pièces jointes
- Filtrage et recherche avancée
- Documentation Swagger 
- Support Docker
- Tests unitaires et d’intégration
