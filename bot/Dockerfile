FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY target/bot-0.0.1.jar app.jar

EXPOSE 8080
EXPOSE 9090

RUN adduser -D link-tracker
USER link-tracker

ENTRYPOINT ["java","-jar","app.jar"]
