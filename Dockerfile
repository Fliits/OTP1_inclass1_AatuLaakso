FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY . /app
RUN mvn package
CMD ["mvn", "test"]