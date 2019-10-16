FROM maven:3.6.2-jdk-8
MAINTAINER weather

COPY src /home/app/src
COPY pom.xml /home/app/pom.xml

WORKDIR /home/app/

RUN mvn -f /home/app/pom.xml clean package

COPY config/*.json ./config/

ENTRYPOINT ["java", "-server", "-jar", "/home/app/target/weather-1.0.0-SNAPSHOT-fat.jar", "-conf", "./config/config.json"]
