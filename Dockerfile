FROM maven:3.9-eclipse-temurin-21-alpine AS build-li-lib
RUN apk add git
RUN mkdir -p /usr/src/build
RUN git clone https://github.com/sipgate/li-lib-x1x2x3.git /usr/src/build/li-lib-x1x2x3
WORKDIR /usr/src/build/li-lib-x1x2x3
RUN git checkout 1.0.0-RELEASE && mvn -DskipTests clean install

FROM maven:3.9-eclipse-temurin-21-alpine AS build-simulator
COPY --from=build-li-lib /root/.m2 /root/.m2

# download dependencies
RUN mkdir -p /usr/src/build /app
COPY pom.xml /usr/src/build
WORKDIR /usr/src/build

# compile the jar
COPY src /usr/src/build/src
ARG MVN_ADDITIONAL_ARGS
RUN mvn -B \
    -P spring-boot-application \
    clean package $MVN_ADDITIONAL_ARGS

FROM eclipse-temurin:21-alpine AS runtime
RUN mkdir /app
WORKDIR /app

COPY --from=build-simulator /usr/src/build/target/simulator.jar /app
COPY --chmod=0775 docker/simulator/start.sh /app/start.sh

EXPOSE 8080

CMD ["sh", "-c", "/app/start.sh"]
