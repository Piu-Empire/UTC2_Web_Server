# UTC2 Backend

Spring Boot backend cho ứng dụng UTC2.

## Yêu cầu
- Java 21
- MySQL 8+
- Maven 3.8+

## Chạy project

1. Tạo database MySQL:
```sql
CREATE DATABASE utc2_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Cập nhật `src/main/resources/application.properties`:
   - `spring.datasource.username`
   - `spring.datasource.password`
   - `app.jwt.secret`

3. Chạy server:
```bash
./mvnw spring-boot:run
```

4. Swagger UI: http://localhost:8080/swagger-ui.html

## Cấu trúc
- `config/` - Cấu hình Security, CORS, Swagger
- `security/` - JWT, UserDetails
- `middleware/` - Filter request
- `common/` - Constants, Enums, Utils, Response
- `exception/` - Exception handling
- `modules/` - Business logic (auth, profile, academic, finance, dormitory, interaction)
