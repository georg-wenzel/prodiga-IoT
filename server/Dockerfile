FROM maven:3-jdk-8
RUN useradd -u 1001 -U -m prodiga_user

RUN mkdir -p /home/prodiga_user/app
WORKDIR /home/prodiga_user/app

COPY pom.xml ./
COPY src ./src
RUN echo "spring.profiles.active=docker" >> src/main/resources/application.properties

RUN chown -R prodiga_user:prodiga_user .
USER prodiga_user

ENTRYPOINT ["mvn"]
CMD ["spring-boot:run"]

RUN mvn dependency:go-offline

EXPOSE 8080/tcp