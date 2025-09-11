FROM maven:3.9.11-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

COPY src ./src

EXPOSE 8080 35729

CMD ["mvn", "spring-boot:run"]