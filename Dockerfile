FROM maven:3.9-eclipse-temurin-21-alpine AS build-simulator

RUN mkdir -p /usr/src/build /app
COPY pom.xml /usr/src/build
COPY src /usr/src/build/src
WORKDIR /usr/src/build

ARG MVN_ADDITIONAL_ARGS
RUN mvn -B \
    -P spring-boot-application \
    clean package $MVN_ADDITIONAL_ARGS

FROM eclipse-temurin:21-alpine AS runtime

# Distrust the compromised emSign ECC Root CA - G3 and Comodo AAA Services roots (eMudhra intermediate CA compromise,
# 2026-10) in the system trust store and in the JDK's bundled cacerts. See https://emudhra.we-have-a-problem.com/#linux
RUN set -eu \
 && apk add --no-cache ca-certificates \
 && sed -i -e 's|^mozilla/emSign_ECC_Root_CA_-_G3.crt|!&|' -e 's|^mozilla/Comodo_AAA_Services_root.crt|!&|' /etc/ca-certificates.conf \
 && update-ca-certificates \
 && cacerts="$(dirname "$(dirname "$(readlink -f "$(command -v java)")")")/lib/security/cacerts" \
 && keytool -list -v -keystore "$cacerts" -storepass changeit \
      | awk '/^Alias name: /{alias=substr($0,13)} /^Owner: .*CN=(AAA Certificate Services|emSign ECC Root CA - G3)(,|$)/{print alias}' \
      | while read -r alias; do keytool -delete -alias "$alias" -keystore "$cacerts" -storepass changeit; done \
 && ! keytool -list -v -keystore "$cacerts" -storepass changeit | grep -qE '^Owner: .*CN=(AAA Certificate Services|emSign ECC Root CA - G3)(,|$)'
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
