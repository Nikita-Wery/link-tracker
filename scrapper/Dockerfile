FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY target/scrapper-0.0.1.jar app.jar

EXPOSE 8081
EXPOSE 9091

RUN adduser -D link-tracker
USER link-tracker

ENTRYPOINT ["java","-jar","app.jar"]
