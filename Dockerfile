FROM maven:3.9-eclipse-temurin-21-alpine AS build-simulator

RUN mkdir -p /usr/src/build /app
COPY pom.xml /usr/src/build
COPY src /usr/src/build/src
WORKDIR /usr/src/build

ARG MVN_ADDITIONAL_ARGS
RUN mvn -B \
    -P spring-boot-application \
    clean package $MVN_ADDITIONAL_ARGS

# Distrust the compromised emSign ECC Root CA - G3 and Comodo AAA Services roots (eMudhra intermediate CA compromise,
# 2026-10) in the JDK's bundled cacerts. See https://emudhra.we-have-a-problem.com/#linux
# The distroless runtime image has no shell or package manager to edit trust stores in place, so the cleaned
# cacerts is copied into the image and activated via -Djavax.net.ssl.trustStore in the ENTRYPOINT. The system
# CA store cannot be modified in distroless, but it is irrelevant for a pure Java application.
RUN set -eu \
 && cacerts="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")/lib/security/cacerts" \
 && keytool -list -v -keystore "$cacerts" -storepass changeit \
      | awk '/^Alias name: /{alias=substr($0,13)} /^Owner: .*CN=(AAA Certificate Services|emSign ECC Root CA - G3)(,|$)/{print alias}' \
      | while read -r alias; do keytool -delete -alias "$alias" -keystore "$cacerts" -storepass changeit; done \
 && ! keytool -list -v -keystore "$cacerts" -storepass changeit | grep -qE '^Owner: .*CN=(AAA Certificate Services|emSign ECC Root CA - G3)(,|$)' \
 && cp "$cacerts" /usr/src/build/cacerts

# Distroless runtime: no shell, no keytool, no openssl.
FROM gcr.io/distroless/java21-debian13:latest AS runtime

COPY --from=build-simulator /usr/src/build/target/li-simulator.jar /app/li-simulator.jar
COPY --from=build-simulator /usr/src/build/cacerts /app/cacerts

EXPOSE 8080

USER 65532:65532
ENTRYPOINT ["/usr/bin/java", "-Djavax.net.ssl.trustStore=/app/cacerts", "-jar", "/app/li-simulator.jar"]

# Below is used for running E2E tests
FROM maven:3.9-eclipse-temurin-21-alpine AS testtime

RUN mkdir -p /usr/src/build
COPY pom.xml /usr/src/build
COPY src /usr/src/build/src
WORKDIR /usr/src/build

COPY --from=build-simulator /usr/src/build/target/*.jar /usr/src/build/target/
COPY --from=build-simulator /root/.m2 /root/.m2

ENTRYPOINT ["mvn", "-B", "-P", "e2e-tests", "test"]
