FROM registry.sipgate.net/docker/maven:3.9-eclipse-temurin-21-alpine AS build

# download dependencies
RUN apk add openssl
RUN mkdir -p /usr/src/build /app
COPY pom.xml /usr/src/build
COPY maven_settings.xml /usr/src/build
WORKDIR /usr/src/build
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B \
    -P spring-boot-application \
    -s maven_settings.xml \
    de.qaware.maven:go-offline-maven-plugin:resolve-dependencies

# compile the jar
COPY src /usr/src/build/src
ARG MVN_ADDITIONAL_ARGS
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B \
    -P spring-boot-application \
    -s maven_settings.xml \
    clean package $MVN_ADDITIONAL_ARGS

COPY --chmod=0775 docker/simulator/start.sh /app/start.sh

EXPOSE 8080

CMD ["/app/start.sh"]
