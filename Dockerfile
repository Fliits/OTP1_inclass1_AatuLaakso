FROM maven:3.9-eclipse-temurin-25

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libgtk-3-0 \
        libxtst6 \
        libx11-xcb1 \
        libgl1 \
        xvfb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
COPY init.sql .

RUN mvn -q clean package -DskipTests

ENV DB_HOST=host.minikube.internal
ENV DB_PORT=3306
ENV DB_NAME=temperature_converter
ENV DB_USER=root
ENV DB_PASSWORD=

ENTRYPOINT ["xvfb-run", "--auto-servernum", "--server-args=-screen 0 1280x800x24", "mvn", "-q", "javafx:run"]