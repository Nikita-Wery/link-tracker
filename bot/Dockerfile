FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/bot-0.0.1.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
