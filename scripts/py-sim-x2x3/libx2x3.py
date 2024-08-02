#!usr/bin/env python

import struct
from enum import IntEnum
from typing import Optional
from dataclasses import dataclass
import uuid


class PduType(IntEnum):
    X2 = 1
    X3 = 2


class Direction(IntEnum):
    TO_TRG = 2
    FROM_TRG = 3


class PayloadFormat(IntEnum):
    RTP = 8


@dataclass
class XMessage:
    version: int
    pdu_type: PduType
    header_length: int
    payload_length: int
    payload_format: PayloadFormat
    payload_direction: Direction
    xid: uuid.UUID
    correlation_id: int
    conditional_attributes: Optional[bytes] = None  # None if not determined yet
    payload: Optional[bytes] = None  # None if not determined yet

    @classmethod
    def decode_fixed_header(cls, data):
        """decode a packet from the first 40 bytes. the variable length fields are set to None."""
        assert len(data) == 40
        (
            version,
            pdu_type,
            header_length,
            payload_length,
            payload_format,
            payload_direction,
            xid_bytes,
            correlation_id,
        ) = struct.unpack("!HHIIHH16sQ", data)

        xid = uuid.UUID(bytes=xid_bytes)
        conditional_attributes = b""
        payload = b""

        return cls(
            version,
            PduType(pdu_type),
            header_length,
            payload_length,
            PayloadFormat(payload_format),
            Direction(payload_direction),
            xid,
            correlation_id,
            conditional_attributes,
            payload,
        )

    def encode(self):
        header = struct.pack(
            "!HHI I H H 16s Q",
            self.version,
            self.pdu_type.value,
            self.header_length,
            self.payload_length,
            self.payload_format.value,
            self.payload_direction.value,
            self.xid.bytes,
            self.correlation_id,
        )
        return header + self.conditional_attributes + self.payload

    def __str__(self):
        attrs = [
            f"- Version: {self.version}",
            f"- PDU Type: {self.pdu_type.name}",
            f"- Header Length: {self.header_length}",
            f"- Payload Length: {self.payload_length}",
            f"- Payload Format: {self.payload_format.name}",
            f"- Payload Direction: {self.payload_direction.name}",
            f"- XID: {self.xid}",
            f"- Correlation ID: {self.correlation_id}",
        ]

        if self.conditional_attributes is not None:
            attrs.append(f"- Attributes: len({len(self.conditional_attributes)})")

        if self.payload is not None:
            attrs.append(f"- Payload: len({len(self.payload)})")

        return "\n".join(attrs)
