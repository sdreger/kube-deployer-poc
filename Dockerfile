FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine
RUN addgroup -S spring-users && adduser -S spring-user -G spring-users
USER spring-user:spring-users
EXPOSE 8080
WORKDIR /application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} /application/app.jar
ENTRYPOINT ["java","-jar","/application/app.jar"]
