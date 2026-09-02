FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY common-module common-module
COPY customer-module customer-module
COPY account-module account-module
COPY transfer-module transfer-module
COPY digibank-web digibank-web
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S digibank && adduser -S -G digibank digibank
WORKDIR /app
COPY --from=build /workspace/digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar app.jar
USER digibank
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
