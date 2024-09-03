#!/bin/bash

set -eu

STORES_PATH=/tmp

echo "Cleaning up old truststore..."
if [ -f $STORES_PATH/truststore.jks ]; then
  rm $STORES_PATH/truststore.jks
fi

echo "Cleaning up old keystore..."
if [ -f $STORES_PATH/keystore.p12 ]; then
  rm $STORES_PATH/keystore.p12
fi

NETWORK_ELEMENT_CA_PATH="${NETWORK_ELEMENT_CA_PATH:-/mutual-tls-stores/ca-certs/network-element-ca.crt}"

echo "Importing CA certificate file..."
keytool \
  -import \
  -storetype jks \
  -noprompt \
  -trustcacerts \
  -alias network-element-ca.crt \
  -file "${NETWORK_ELEMENT_CA_PATH}" \
  -keystore $STORES_PATH/truststore.jks \
  -storepass changeit

echo "Creating PKCS12 keystore..."

openssl pkcs12 \
  -export \
  -in "/mutual-tls-stores/certs/simulator.crt" \
  -inkey "/mutual-tls-stores/keys/simulator.key" \
  -out $STORES_PATH/keystore.p12 \
  -name simulator \
  -passout pass:changeit

exec java -jar /usr/src/build/target/simulator.jar
