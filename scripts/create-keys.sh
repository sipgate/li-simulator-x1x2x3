#!/bin/bash

set -e
set -u

SCRIPT_DIR=`dirname $0`
OUT_DIR=${SCRIPT_DIR}/../docker/network-element

openssl ecparam -name prime256v1 -genkey -noout -out ${OUT_DIR}/ca.key
openssl req -new -x509 -sha256 -key ${OUT_DIR}/ca.key -out ${OUT_DIR}/ca.crt -config ${SCRIPT_DIR}/keys/ca-cert.conf

openssl ecparam -name prime256v1 -genkey -noout -out ${OUT_DIR}/server.key
openssl req -new -sha256 -key ${OUT_DIR}/server.key -out ${OUT_DIR}/server.csr -config ${SCRIPT_DIR}/keys/server-cert.conf
openssl x509 -req -in ${OUT_DIR}/server.csr -CA ${OUT_DIR}/ca.crt -CAkey ${OUT_DIR}/ca.key -CAcreateserial -out ${OUT_DIR}/server.crt -days 1000 -sha256 -extfile ${SCRIPT_DIR}/keys/server-cert.conf -extensions v3_ca
openssl pkcs12 -export -out ${OUT_DIR}/server.pfx -inkey ${OUT_DIR}/server.key -in ${OUT_DIR}/server.crt -passout pass:password

openssl ecparam -name prime256v1 -genkey -noout -out ${OUT_DIR}/client1.key
openssl req -new -sha256 -key ${OUT_DIR}/client1.key -out ${OUT_DIR}/client1.csr -config ${SCRIPT_DIR}/keys/client-cert.conf
openssl x509 -req -in ${OUT_DIR}/client1.csr -CA ${OUT_DIR}/ca.crt -CAkey ${OUT_DIR}/ca.key -CAcreateserial -out ${OUT_DIR}/client1.crt -days 1000 -sha256 -extfile ${SCRIPT_DIR}/keys/client-cert.conf -extensions v3_ca
# incompatible with java: openssl pkcs12 -export -out ca.pfx -nokeys -in ca.crt
keytool -importcert -keystore ${OUT_DIR}/ca.pfx -file ${OUT_DIR}/ca.crt -noprompt -storepass password
openssl pkcs12 -export -out ${OUT_DIR}/client1.pfx -inkey ${OUT_DIR}/client1.key -in ${OUT_DIR}/client1.crt -passout pass:password


# curl --cacert ca.crt -E client1.crt --key client1.key https://localhost/X1/NE --data "ListAllDetailsRequest"
