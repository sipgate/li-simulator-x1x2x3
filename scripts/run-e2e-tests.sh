#!/usr/bin/env bash

# Run end-to-end tests
#
# Usage: ./scripts/run-e2e-tests.sh

set -e

docker compose up -d --build

SERVICE_HOST="${SERVICE_HOST:-simulator}"
SERVICE_PORT="${SERVICE_PORT:-8080}"
WIREMOCK_HOST="${WIREMOCK_HOST:-network-element}"
WIREMOCK_PORT="${WIREMOCK_PORT:-8080}"

DOCKER_NETWORK_ARG="--network li-network"

# don't exit on error to be able to shutdown the running docker containers
set +e

docker run ${DOCKER_NETWORK_ARG} \
  --rm \
  -t \
  -u "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven/.m2 \
  -v "$(pwd)":/usr/src/build \
  -w /usr/src/build \
  maven:3.9-eclipse-temurin-21-alpine \
  mvn -B -s maven_settings.xml test -Pe2e-tests \
  -Duser.home=/tmp/maven \
  -DserviceHost="${SERVICE_HOST}" \
  -DservicePort="${SERVICE_PORT}" \
  -DwiremockHost="${WIREMOCK_HOST}" \
  -DwiremockPort="${WIREMOCK_PORT}" \
  -Djdk.httpclient.HttpClient.log=requests

# remember the result for the exit code of this script
TEST_RESULT=$?
set -e

docker compose down

exit $TEST_RESULT
