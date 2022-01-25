<<<<<<< HEAD
FROM adoptopenjdk/openjdk11:alpine-jre
=======
FROM adoptopenjdk/openjdk11:jre
>>>>>>> 7b826e03be0e3aacca1c3c98165bf481267d1bfd
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
