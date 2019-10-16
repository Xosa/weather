FROM openjdk:8-jdk-alpine
MAINTAINER weather

WORKDIR /app/

COPY target/*-fat.jar ./weather-server.jar
COPY config/*.json ./config/

ENTRYPOINT ["java", "-server", "-jar", "./weather-server.jar", "-conf", "./config/config.json"]
