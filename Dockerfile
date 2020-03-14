FROM openjdk:11

RUN apt-get update && apt-get install -y maven
COPY . /prodiga
RUN cd /prodiga && mvn package

ENTRYPOINT ["java", "-jar", "/prodiga/target/prodiga-1.0.0.jar"]