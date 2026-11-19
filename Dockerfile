FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package && cp target/*.war /app/app.war

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.war ./app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
