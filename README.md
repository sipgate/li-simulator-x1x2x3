# li-simulator-x1x2x3

![Alles Wird Besser](https://img.shields.io/badge/ansprechpartner-alleswirdbesser-blue.svg)

Human-friendlier interface to interact with a X1/X2/X3 node. Intended to test the ETSI TS 103 221 implementations of Network Elements.

## Local development

1. `git clone` the repository
2. Run `npm install` for git hooks and prettier (code formatting)

The API documentation is available at `http://localhost:8080/swagger-ui.html`.

## Run end-to-tend tests

Run e2e-tests via `./scripts/run-e2e-tests.sh`

For details about test scenarios within wiremock, see [Network Element README](./docker/network-element/README.md)

## Send x2/x3 packet to simulator

Start the simulator environment using docker. There is a binary x2 file in `src/test/misc/x2-demo-01.bin` that you can copy into the container and then send to the server:

```shell
docker compose cp src/test/misc/x2-demo-01.bin simulator:/tmp/x2-demo-01.bin

docker compose exec -i simulator \
  /bin/bash -c "cat /tmp/x2-demo-01.bin | openssl s_client \
    -connect 127.0.0.1:42069 \
    -cert /mutual-tls-stores/certs/network-element.crt \
    -key /mutual-tls-stores/keys/network-element.key"
```

## Further information

- EVE Explains: ETSI TS 103 221 - X1/X2/X3 https://www.lawfulinterception.com/explains/etsi-ts-103-221/
