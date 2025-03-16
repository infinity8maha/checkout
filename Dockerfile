FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY . .
RUN ./gradlew bootJar
EXPOSE 8080
ENTRYPOINT ["java","-jar","build/libs/*.jar"] 