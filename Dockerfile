FROM eclipse-temurin:21-jdk

WORKDIR /app

# Đảm bảo bạn đã chạy 'mvn package' hoặc './gradlew build' trước khi build docker
COPY target/*.jar app.jar

# Tạo thư mục log để không bị lỗi quyền ghi
RUN mkdir -p logs

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]