FROM maven:3.9.3-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=/workspace/target/password-manager-v2.jar
COPY --from=build /workspace/target/*.jar /app/password-manager-v2.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/password-manager-v2.jar"]
