#!/usr/bin/env bash

# Run end-to-end tests
#
# Usage: ./scripts/run-e2e-tests.sh

set -e

docker compose up -d --build

SERVICE_HOST="${SERVICE_HOST:-simulator}"
SERVICE_PORT="${SERVICE_PORT:-8080}"

DOCKER_NETWORK_ARG="--network li-network"

docker run ${DOCKER_NETWORK_ARG} \
  --rm \
  -t \
  -u "$(id -u):$(id -g)" \
  -e MAVEN_CONFIG=/tmp/maven/.m2 \
  -v "$(pwd)":/usr/src/build \
  -w /usr/src/build \
  registry.sipgate.net/docker/maven:3.9-eclipse-temurin-21-alpine \
  mvn -B -s maven_settings.xml test -Pe2e-tests \
  -Duser.home=/tmp/maven \
  -DserviceHost="${SERVICE_HOST}" \
  -DservicePort="${SERVICE_PORT}" \
  -Djdk.httpclient.HttpClient.log=requests
