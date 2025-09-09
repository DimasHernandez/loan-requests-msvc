#FROM eclipse-temurin:21-jdk-alpine
#VOLUME /tmp
#COPY *.jar request-msvc.jar
#ENV JAVA_OPTS=" -Xshareclasses:name=cacheapp,cacheDir=/cache,nonfatal -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
## Replace with a non-root user to avoid running the container with excessive privileges
#USER appuser
#ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS  -jar request-msvc.jar" ]

# --------------------------------------------------------------------------------------------------------------------------------------------------------------
# Docker image Authentication-msvc
# Stage 1 -> Build the .jar
FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app
COPY settings.gradle build.gradle gradlew main.gradle /app/
COPY gradle /app/gradle
COPY applications /app/applications
COPY domain /app/domain
COPY infrastructure /app/infrastructure
RUN gradle :app-service:clean :app-service:build -x validateStructure --no-daemon

# Stage 2 -> Create final image
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=builder app/applications/app-service/build/libs/request-msvc.jar request-msvc.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "request-msvc.jar"]
