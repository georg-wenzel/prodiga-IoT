FROM maven:3-jdk-8
RUN addgroup --gid 235 prodiga && adduser -disabled-password --gid 235 prodiga_user && chmod -R g=rwx /home/prodiga_user
USER prodiga_user
WORKDIR /home/prodiga_user/app
ENTRYPOINT ["mvn", "spring-boot:run"]