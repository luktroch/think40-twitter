FROM openjdk:8

ARG JAR_FILE

COPY ${JAR_FILE} /twitter/twitter.jar
CMD ["java", "-jar", "/twitter/twitter.jar"]

EXPOSE 8080