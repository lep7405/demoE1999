# Sử dụng OpenJDK 21 làm base image
FROM openjdk:21-jdk-slim


# Thiết lập thư mục làm việc trong container
WORKDIR /app

# Sao chép các file Maven vào thư mục /app
COPY target/*.jar app.jar

# Cổng mà ứng dụng Spring Boot chạy
EXPOSE 8090

# Lệnh để chạy ứng dụng khi container được khởi động
CMD ["java", "-jar", "app.jar"]
