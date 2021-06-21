FROM adoptopenjdk/openjdk11

ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Configure the JAVA_OPTIONS, you can add -XshowSettings:vm to also display the heap size.
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

COPY target/lib/* /deployments/lib/
COPY target/*-runner.jar /deployments/app.jar

EXPOSE 8080
USER 1001

ENTRYPOINT ["java","-jar","/deployments/app.jar"]
