FROM openjdk:11 as extractor
ARG workdir
ARG jarfile
WORKDIR ${workdir:-tmp}
COPY ${jarfile} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM openjdk:11
ARG workdir
ARG appconfig
ARG port
COPY --from=extractor ${workdir:-tmp}/dependencies/ ./
COPY --from=extractor ${workdir:-tmp}/spring-boot-loader/ ./
COPY --from=extractor ${workdir:-tmp}/snapshot-dependencies/ ./
COPY --from=extractor ${workdir:-tmp}/application/ ./
COPY ${appconfig} ./config/application.yml
EXPOSE ${port}
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
