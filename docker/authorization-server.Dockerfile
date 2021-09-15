FROM openjdk:11
ARG workdir
ARG jarfile
WORKDIR ${workdir:-tmp}
COPY ${jarfile} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
