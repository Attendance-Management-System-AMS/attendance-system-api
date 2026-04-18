# Stage 1: Build
FROM maven:3.8.5-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build dự án và bỏ qua chạy test để tối ưu thời gian deploy
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy file jar từ stage build sang. Tên file dựa theo cấu hình trong pom.xml
COPY --from=build /app/target/attendance-system-api-0.0.1-SNAPSHOT.jar app.jar

# Cổng mặc định của Spring Boot
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
