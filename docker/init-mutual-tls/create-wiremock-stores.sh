#!/bin/sh

set -e
set -u

BASE_OUTPUT_PATH=/mutual-tls-stores
ROLE=network-element

echo "Cleaning up old truststore..."
if [ -f $BASE_OUTPUT_PATH/$ROLE-truststore.jks ]; then
  rm $BASE_OUTPUT_PATH/$ROLE-truststore.jks
fi

echo "Cleaning up old keystore..."
if [ -f $BASE_OUTPUT_PATH/$ROLE-keystore.p12 ]; then
  rm $BASE_OUTPUT_PATH/$ROLE-keystore.p12
fi

echo "Importing CA certificate file..."
keytool \
  -import \
  -storetype jks \
  -noprompt \
  -trustcacerts \
  -alias simulator-ca.crt \
  -file $BASE_OUTPUT_PATH/ca-certs/simulator-ca.crt \
  -keystore $BASE_OUTPUT_PATH/$ROLE-truststore.jks \
  -storepass changeit

echo "Importing client certificate file..."
  keytool \
    -import \
    -storetype jks \
    -noprompt \
    -alias simulator.crt \
    -file "$BASE_OUTPUT_PATH/certs/simulator.crt" \
    -keystore $BASE_OUTPUT_PATH/$ROLE-truststore.jks \
    -storepass changeit

echo "Creating PKCS12 keystore..."

openssl pkcs12 \
  -export \
  -in "$BASE_OUTPUT_PATH/certs/$ROLE.crt" \
  -inkey "$BASE_OUTPUT_PATH/keys/$ROLE.key" \
  -out $BASE_OUTPUT_PATH/$ROLE-keystore.p12 \
  -name $ROLE \
  -passout pass:changeit

chmod a+rw "${BASE_OUTPUT_PATH}/${ROLE}-truststore.jks" "${BASE_OUTPUT_PATH}/${ROLE}-keystore.p12"
