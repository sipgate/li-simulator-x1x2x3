#!/bin/bash

set -eu

if [[ -z "$ROLE" ]]; then
  echo "variable ROLE not set!"
  exit 1
fi

if [[ -z "$COMMON_NAME" ]]; then
  echo "variable COMMON_NAME not set!"
  exit 1
fi

BASE_OUTPUT_PATH="/mutual-tls-stores"

if [[ -f "${BASE_OUTPUT_PATH}/${ROLE}_is_ready" ]]; then
  echo "${ROLE} already initialized"
  exit 0
fi

if [[ ! -d "${BASE_OUTPUT_PATH}/ca-certs" ]]; then
  mkdir -p "${BASE_OUTPUT_PATH}/ca-certs"
fi

if [[ ! -d "${BASE_OUTPUT_PATH}/certs" ]]; then
  mkdir -p "${BASE_OUTPUT_PATH}/certs"
fi

if [[ ! -d "${BASE_OUTPUT_PATH}/keys" ]]; then
  mkdir -p "${BASE_OUTPUT_PATH}/keys"
fi

echo "Generating ${ROLE} CA key and crt"
openssl ecparam -name prime256v1 -genkey -noout -out /tmp/${ROLE}-ca.key
openssl req -new -x509 -sha256 -key /tmp/${ROLE}-ca.key -out "${BASE_OUTPUT_PATH}/ca-certs/${ROLE}-ca.crt" -config /init-mtls/ca-cert.conf


echo "generate own key and crt (signed by CA)"
openssl ecparam -name prime256v1 -genkey -noout -out "${BASE_OUTPUT_PATH}/keys/${ROLE}.key"
openssl req -new -sha256 -key "${BASE_OUTPUT_PATH}/keys/${ROLE}.key" -out /tmp/self.csr -subj "/CN=${COMMON_NAME}/OU=Alles Wird Besser/O=sipgate GmbH/L=Duesseldorf/ST=NRW/C=DE"
openssl x509 -req -in /tmp/self.csr -CA "${BASE_OUTPUT_PATH}/ca-certs/${ROLE}-ca.crt" -CAkey /tmp/${ROLE}-ca.key -CAcreateserial -out "${BASE_OUTPUT_PATH}/certs/${ROLE}.crt" -days 3650 -sha256 -extfile /init-mtls/cert.conf -extensions v3_ca

echo "done"

touch "${BASE_OUTPUT_PATH}/${ROLE}_is_ready"
