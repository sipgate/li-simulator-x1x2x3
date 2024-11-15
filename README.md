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

## Retrieving X3 RTP Audio

The simulator can be used to actually dump all RTP audio packets and create an MP3. To do this, send your RTP-Stream via X3 to the simulator on port 42069.
It is out of scope how you do this - use your NE for example.

After you have sent the RTP-Stream, you can retrieve the audio by using the following commands:

```shell
python3 -m venv .venv
source .venv/bin/activate
pip3 install -r scripts/requirements.txt
python3 scripts/download-x3rtp-and-convert.py http://localhost:8080 [XID] [in|out]
```

Replace `[XID]` with the XID of the call you want to retrieve and `[in|out]` with the direction of the call. The script will download the RTP packets and convert them to an MP3 file called `[XID]-[DIRECTION].mp3`.

Be sure to reset the X2X3 in-memory receiver after you have downloaded the audio.

```shell
curl -X POST "http://localhost:8080/x2x3/reset"
```

## Further information

- EVE Explains: ETSI TS 103 221 - X1/X2/X3 https://www.lawfulinterception.com/explains/etsi-ts-103-221/
