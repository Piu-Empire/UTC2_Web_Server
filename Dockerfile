FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml trước để tận dụng Docker cache cho các thư viện
COPY pom.xml .
RUN mvn dependency:go-offline

# Sau đó mới copy toàn bộ mã nguồn vào để build
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p logs

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]
