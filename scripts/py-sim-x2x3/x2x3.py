#!/usr/bin/env python

# stdlib:
import socket
import random
import uuid
import time

# 3rd party:
import click

# local:
from libx2x3 import XMessage, PduType, PayloadFormat, Direction


__version__ = "0.0.1"


def receive_data(host, port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.bind((host, port))
        sock.listen(1)
        print(f"Listening on {host}:{port}")

        conn, addr = sock.accept()
        with conn:
            print(f"Connected by {addr}")
            while True:
                # Versuche, den fixen teil des Headers zu lesen
                fixed_header = conn.recv(40)
                if not fixed_header:
                    break

                # Erstellen Sie eine Message vom fixen teil des headers
                message = XMessage.decode_fixed_header(fixed_header)

                # Lese die bedingten Attributfelder, falls vorhanden
                if message.header_length > 40:
                    message.conditional_attributes = sock.recv(
                        message.header_length - 40
                    )

                # Lese den Payload, falls vorhanden
                message.payload = b""
                while len(message.payload) < message.payload_length:
                    chunk = conn.recv(message.payload_length - len(message.payload))
                    if not chunk:
                        break
                    message.payload += chunk

                # liefer das paket zurück
                yield message


###############
# commands


@click.group()
def cli():
    """X2/X3 Protocol Tools"""
    pass


def enum_type(enum):
    """Create a Click type that converts strings to enum values."""

    def _enum_type(value):
        try:
            return enum[value]
        except KeyError:
            raise click.BadParameter(f"Invalid value for {enum.__name__}: {value}")

    return _enum_type


@click.command(
    name="listen",
    help="X2/X3 Protocol Server for receiving and processing X2/X3 protocol messages.",
)
@click.option(
    "--host", "-h",
    default="localhost",
    help="Server hostname or IP address (default: localhost)",
)
@click.option("--port", "-p", default=7589, help="Server port number (default: 7589)")
@click.option(
    '--forever/--no-forever',
    default=True,
    help="Listen repeatedly (default: True)"
)
def listen(host, port, forever):
    while True:
        for m in receive_data(host, port):
            print(f"Message:\n{m}")
        print()
        if not forever:
            break


@click.command(
    name="send-fake", help="Send random X2/X3 protocol messages to the server."
)
@click.option(
    "--host",
    "-h",
    default="localhost",
    help="Server hostname or IP address (default: localhost)",
)
@click.option("--port", "-p", default=7589, help="Server port number (default: 7589)")
@click.option(
    "--num-packets",
    "-n",
    default=10,
    help="Number of random packets to send (default: 10)",
)
@click.option(
    "--wait-secs", default=0.1, help="Wait time between sending packets (default: 0.1)"
)
@click.option(
    "--payload-format",
    default=PayloadFormat.RTP.name,
    type=enum_type(PayloadFormat),
    help=f"Payload format (default: {PayloadFormat.RTP.name})",
)
@click.option(
    "--pdu-type",
    default=PduType.X3.name,
    type=enum_type(PduType),
    help=f"PDU type (default: {PduType.X3.name})",
)
def send_fake(host, port, num_packets, wait_secs, payload_format, pdu_type):
    correlation_id = random.randint(0, 2**64 - 1)
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.connect((host, port))
        for _ in range(num_packets):
            payload_length = random.randint(0, 1024)
            payload = random.randbytes(payload_length)
            message = XMessage(
                version=5,
                pdu_type=pdu_type,
                header_length=40,
                payload_length=payload_length,
                payload_format=payload_format,
                payload_direction=random.choice([Direction.FROM_TRG, Direction.TO_TRG]),
                xid=uuid.uuid4(),
                correlation_id=correlation_id,
                conditional_attributes=b"",
                payload=payload,
            )

            encoded_message = message.encode()
            print(f"Sending message:\n{message}")
            sock.sendall(encoded_message)
            time.sleep(wait_secs)
    print(f"Sent {num_packets} packets to {host}:{port}")


@click.command(name="version", help="Show the version of the X2/X3 Protocol Client.")
def version():
    print(__version__)


cli.add_command(listen)
cli.add_command(send_fake)
cli.add_command(version)


if __name__ == "__main__":
    cli()
