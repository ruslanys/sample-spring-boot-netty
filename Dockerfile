FROM openjdk:8-jre-alpine

WORKDIR root/

ADD build/libs/spring-boot-*.jar ./application.jar

EXPOSE 8080

CMD java -server -Xmx750M -jar /root/application.jar