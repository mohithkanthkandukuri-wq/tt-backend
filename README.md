# FindIt+ Backend — Lost and Found Management System API

Enterprise Spring Boot backend powering FindIt+, a secure college campus lost-and-found management platform.

## Technology Stack
- **Java 21 LTS**
- **Spring Boot 3.3.5**
- **Spring Security 6** (Stateless HMAC-SHA256 JWT)
- **Spring Data JPA & Hibernate ORM**
- **MySQL 8.0+**
- **Jakarta Bean Validation**
- **JJWT 0.12.6**
- **BCrypt Password Hashing**
- **Apache Maven**

## Database Setup
1. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS lost_found_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Import `database.sql`:
   ```bash
   mysql -u root -p lost_found_db < database.sql
   ```

## Configuration
Edit `src/main/resources/application.properties` or set environment variables:
- `DB_URL` (default: `jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true`)
- `DB_USERNAME` (default: `root`)
- `DB_PASSWORD` (default: `root`)
- `JWT_SECRET`
- `APP_UPLOAD_DIR` (default: `uploads`)
- `FRONTEND_URL` (default: `http://localhost:5173,http://localhost:3000`)

## Running the Application
```bash
mvn clean package -DskipTests
java -jar target/lostfound-0.0.1-SNAPSHOT.jar
```
Or directly:
```bash
mvn spring-boot:run
```

Server runs on: `http://localhost:8080`  
API Base: `http://localhost:8080/api`

## Seed Accounts
- **Admin**: `admin@findit.edu` / `Admin@123`
- **Student**: `student@findit.edu` / `Student@123`

## Running Tests
```bash
mvn test
```

## API Testing
Import `postman_collection.json` into Postman to test all endpoints.
