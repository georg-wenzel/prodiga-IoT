FROM maven:3-jdk-8
RUN useradd -rm -d /home/prodiga_user -s /bin/bash -u 1000 prodiga_user
USER prodiga_user
WORKDIR /home/prodiga_user/app
ENTRYPOINT ["mvn", "spring-boot:run"]