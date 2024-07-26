# li-simulator-x1x2x3

![Alles Wird Besser](https://img.shields.io/badge/ansprechpartner-alleswirdbesser-blue.svg)

Human-friendlier interface to interact with a X1/X2/X3 node. Intended to test the ETSI TS 103 221 implementations of Network Elements.

## Local development

1. `git clone` the repository
2. Run `npm install` for git hooks and prettier (code formatting)
3. Generate certificates: `./scripts/create-keys.sh`
4. Start the local environment: `docker compose up --build -d`
5. Run e2e-tests via `./scripts/run-e2e-tests.sh`

The API documentation is available at `http://localhost:8080/swagger-ui.html`.

## Further information

- EVE Explains: ETSI TS 103 221 - X1/X2/X3 https://www.lawfulinterception.com/explains/etsi-ts-103-221/
