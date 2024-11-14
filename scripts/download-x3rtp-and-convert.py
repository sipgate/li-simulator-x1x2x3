#!/usr/bin/python3

import subprocess
import sys

# 3rd party
import requests


PAYLOAD_TYPE_TO_ACODEC = {
    "CN": "comfort_noise",
    "CelB": "celb",
    "DVI4": "adpcm_dvi4",
    "G.722": "g722",     # the only tested one, the other ones are AI-guesses
    "G.728": "g728",
    "G.729": "g729",
    "G723": "g723_1",
    "GSM": "gsm",
    "H.261": "h261",
    "H.263": "h263",
    "JPEG": "mjpeg",
    "L16": "pcm_s16be",
    "LPC": "lpc",
    "MP2T": "mpeg2ts",
    "MPA": "mp2",
    "MPV": "mpeg1video",
    "PCMA": "pcm_alaw",
    "PCMU": "pcm_mulaw",
    "QCELP": "qcelp",
    "nv": "nv",
}



def main(sim_url, xid, direction):
    # GET from simurl with request path
    resp = requests.get(f"{sim_url}/x2x3/all/rtp/{xid}/{direction}")
    # get body of response as byte string
    data = resp.content
    # get header "X-Payload-Types"
    payload_types = resp.headers["X-Payload-Types"]
    print("Payload Types: ", payload_types)
    RAW_FILE_NAME = f"{xid}-{direction}.raw"
    MP3_FILE_NAME = f"{xid}-{direction}.mp3"
    with open(RAW_FILE_NAME, "wb") as f:
        f.write(data)
    print(f"Saved {RAW_FILE_NAME}")
    if "," in payload_types:
        print("[WARN] Multiple payload types detected, can not convert to mp3")
        return
    # call ffmpeg to mp3: "   ffmpeg -acodec g722 -f g722  -i in.raw in.mp3"
    ffmeg_acodec = PAYLOAD_TYPE_TO_ACODEC[payload_types]
    ffmeg_format = PAYLOAD_TYPE_TO_ACODEC[payload_types]
    cmd = ["ffmpeg",
        "-acodec", ffmeg_acodec,
        "-f", ffmeg_format,
        "-i", RAW_FILE_NAME,
        MP3_FILE_NAME]
    print("Executing:", " ".join(cmd))
    subprocess.run(cmd, check=True)
    print(f"Converted {RAW_FILE_NAME} to {MP3_FILE_NAME}")


if __name__ == "__main__":
    if len(sys.argv) != 4:
        print(f"Usage: {sys.argv[0]} <sim-url> <xid> <direction>")
        sys.exit(1)
    sim_url = sys.argv[1]
    xid = sys.argv[2]
    direction = sys.argv[3]
    main(sim_url, xid, direction)

