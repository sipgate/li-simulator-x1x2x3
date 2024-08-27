#!/usr/bin/env bash

echo "Cleaning up old truststore..."
if [ -f /mutual-tls-stores/network-element-truststore.jks ]; then
  rm /mutual-tls-stores/network-element-truststore.jks
fi

echo "Cleaning up old keystore..."
if [ -f /mutual-tls-stores/network-element-keystore.p12 ]; then
  rm /mutual-tls-stores/network-element-keystore.p12
fi

echo "Importing CA certificate file..."
keytool \
  -import \
  -storetype jks \
  -noprompt \
  -trustcacerts \
  -alias simulator-ca.crt \
  -file /mutual-tls-stores/ca-certs/simulator-ca.crt \
  -keystore /mutual-tls-stores/network-element-truststore.jks \
  -storepass changeit

echo "Importing client certificate file..."
  keytool \
    -import \
    -storetype jks \
    -noprompt \
    -alias simulator.crt \
    -file "/mutual-tls-stores/certs/simulator.crt" \
    -keystore /mutual-tls-stores/network-element-truststore.jks \
    -storepass changeit

echo "Creating PKCS12 keystore..."

openssl pkcs12 \
  -export \
  -in "/mutual-tls-stores/certs/network-element.crt" \
  -inkey "/mutual-tls-stores/keys/network-element.key" \
  -out /mutual-tls-stores/network-element-keystore.p12 \
  -name network-element \
  -passout pass:changeit
