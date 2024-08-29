#!/bin/bash

set -eu

echo "Cleaning up old truststore..."
if [ -f /app/truststore.jks ]; then
  rm /app/truststore.jks
fi

echo "Cleaning up old keystore..."
if [ -f /app/keystore.p12 ]; then
  rm /app/keystore.p12
fi

NETWORK_ELEMENT_CA_PATH="${NETWORK_ELEMENT_CA_PATH:-/mutual-tls-stores/ca-certs/network-element-ca.crt}"
NETWORK_ELEMENT_CERT_PATH="${NETWORK_ELEMENT_CERT_PATH:-/mutual-tls-stores/certs/network-element.crt}"

echo "Importing CA certificate file..."
keytool \
  -import \
  -storetype jks \
  -noprompt \
  -trustcacerts \
  -alias network-element-ca.crt \
  -file "${NETWORK_ELEMENT_CA_PATH}" \
  -keystore /app/truststore.jks \
  -storepass changeit

echo "Importing client certificate file..."
  keytool \
    -import \
    -storetype jks \
    -noprompt \
    -alias network-element.crt \
    -file "${NETWORK_ELEMENT_CERT_PATH}" \
    -keystore /app/truststore.jks \
    -storepass changeit

echo "Creating PKCS12 keystore..."

openssl pkcs12 \
  -export \
  -in "/mutual-tls-stores/certs/simulator.crt" \
  -inkey "/mutual-tls-stores/keys/simulator.key" \
  -out /app/keystore.p12 \
  -name simulator \
  -passout pass:changeit

exec java -jar /usr/src/build/target/simulator.jar
