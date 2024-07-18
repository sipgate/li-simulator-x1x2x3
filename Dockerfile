FROM registry.sipgate.net/docker/maven:3.9-eclipse-temurin-21-alpine AS build

# download maven dependencies
RUN mkdir -p /usr/src/build
COPY pom.xml /usr/src/build
COPY maven_settings.xml /usr/src/build
WORKDIR /usr/src/build
RUN --mount=type=cache,target=/root/.m2 mvn -B -s maven_settings.xml de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

# compile the jar
COPY src /usr/src/build/src
ARG MVN_ADDITIONAL_ARGS
RUN --mount=type=cache,target=/root/.m2 mvn -B -s maven_settings.xml clean package $MVN_ADDITIONAL_ARGS

FROM registry.sipgate.net/docker/sipgate/runtime/zre-21:debian-bookworm
RUN mkdir /app
COPY --from=build /usr/src/build/target/simulator.jar /app

EXPOSE 3469
CMD ["java", "-jar", "/app/simulator.jar"]
