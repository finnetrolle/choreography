FROM java:8-jre

ADD ./target/crossroad.jar /app/
CMD ["java", "-Xmx900m", "-jar", "/app/crossroad.jar"]

EXPOSE 8080