FROM maven:3.6.3

COPY . /project
RUN cd /project && mvn clean package

ENTRYPOINT ["java", "-jar", "/project/target/RowMatch-0.0.1-SNAPSHOT.jar"]