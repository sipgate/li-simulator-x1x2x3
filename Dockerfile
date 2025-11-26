FROM maven:3.9-eclipse-temurin-21-alpine AS build-simulator

RUN mkdir -p /usr/src/build /app
COPY pom.xml /usr/src/build
COPY src /usr/src/build/src
WORKDIR /usr/src/build

ARG MVN_ADDITIONAL_ARGS
RUN mvn -B \
    -P spring-boot-application \
    clean package $MVN_ADDITIONAL_ARGS

FROM eclipse-temurin:25-alpine AS runtime
RUN mkdir /app
WORKDIR /app

COPY --from=build-simulator /usr/src/build/target/li-simulator.jar /app
COPY --chmod=0775 docker/simulator/start.sh /app/start.sh

EXPOSE 8080

CMD ["sh", "-c", "/app/start.sh"]

# Below is used for running E2E tests
FROM maven:3.9-eclipse-temurin-21-alpine AS testtime

RUN mkdir -p /usr/src/build
COPY pom.xml /usr/src/build
COPY src /usr/src/build/src
WORKDIR /usr/src/build

COPY --from=build-simulator /usr/src/build/target/*.jar /usr/src/build/target/
COPY --from=build-simulator /root/.m2 /root/.m2

ENTRYPOINT ["mvn", "-B", "-P", "e2e-tests", "test"]
