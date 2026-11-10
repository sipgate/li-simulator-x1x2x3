#!/bin/sh
#
# Builds the Java TLS stores for a role from the certificates in the shared
# /mutual-tls-stores volume, so that the distroless runtime containers can
# consume them directly (they have no shell/keytool/openssl to do it at startup).
#
# Required environment:
#   ROLE     whose certificate/key to bundle (e.g. "simulator")
# Optional environment:
#   CA_PATH  CA certificate to trust (defaults to the network-element CA)

set -eu

if [ -z "${ROLE:-}" ]; then
  echo "variable ROLE not set!"
  exit 1
fi

BASE_OUTPUT_PATH="/mutual-tls-stores"
CA_PATH="${CA_PATH:-${BASE_OUTPUT_PATH}/ca-certs/network-element-ca.crt}"

echo "Cleaning up old truststore..."
rm -f "${BASE_OUTPUT_PATH}/${ROLE}-truststore.jks"

echo "Cleaning up old keystore..."
rm -f "${BASE_OUTPUT_PATH}/${ROLE}-keystore.p12"

echo "Importing CA certificate file ${CA_PATH}..."
keytool \
  -import \
  -storetype jks \
  -noprompt \
  -trustcacerts \
  -alias "$(basename "${CA_PATH}")" \
  -file "${CA_PATH}" \
  -keystore "${BASE_OUTPUT_PATH}/${ROLE}-truststore.jks" \
  -storepass changeit

echo "Creating PKCS12 keystore..."
openssl pkcs12 \
  -export \
  -in "${BASE_OUTPUT_PATH}/certs/${ROLE}.crt" \
  -inkey "${BASE_OUTPUT_PATH}/keys/${ROLE}.key" \
  -out "${BASE_OUTPUT_PATH}/${ROLE}-keystore.p12" \
  -name "${ROLE}" \
  -passout pass:changeit

# the distroless runtime containers run as non-root and must be able to read the stores
chmod a+rw "${BASE_OUTPUT_PATH}/${ROLE}-truststore.jks" "${BASE_OUTPUT_PATH}/${ROLE}-keystore.p12"
