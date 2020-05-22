FROM maven:3-jdk-8
RUN groupadd -g 62035 prodiga
RUN useradd -g 62035 -l -m -s /bin/false -u 62035 prodiga_user
USER prodiga_user
WORKDIR /home/prodiga_user/app
ENTRYPOINT ["mvn", "spring-boot:run"]
