package com.sipgate.li.simulator.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.lib.x2x3.protocol.PayloadDirection;
import com.sipgate.li.lib.x2x3.protocol.PayloadFormat;
import com.sipgate.li.lib.x2x3.protocol.PduObject;
import com.sipgate.li.lib.x2x3.protocol.PduType;
import com.sipgate.li.lib.x2x3.protocol.tlv.TLV;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class X2X3ControllerTest {

  private final PduObject pdu = new PduObject(
    (short) 0,
    (short) 5,
    PduType.X3_PDU,
    PayloadFormat.RTP,
    PayloadDirection.SENT_FROM_TARGET,
    UUID.fromString("62cd13da-7797-468c-befd-77f05008c996"),
    new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 },
    new TLV[0],
    new byte[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 }
  );

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "base64" })
  void it_formats_pdu_to_base64(final String format) {
    final var actual = X2X3Controller.formatPdu(pdu, format);
    assertThat(actual).isEqualTo("AAUAAgAAACgAAAAQAAgAA2LNE9p3l0aMvv138FAIyZYAAQIDBAUGBwIDBQcLDRETFx0fJSkrLzU=");
  }

  @Test
  void it_formats_pdu_to_java() {
    final var actual = X2X3Controller.formatPdu(pdu, "java");
    final var fixedActual = actual.replaceAll("@\\w+", "@(addr)");
    assertThat(fixedActual).isEqualTo(
      "PduObject[majorVersion=0, minorVersion=5, pduType=X3_PDU, payloadFormat=RTP, payloadDirection=SENT_FROM_TARGET, xid=62cd13da-7797-468c-befd-77f05008c996, correlationID=[B@(addr), conditionalAttributeFields=[Lcom.sipgate.li.lib.x2x3.protocol.tlv.TLV;@(addr), payload=[B@(addr)]"
    );
  }

  @Test
  void it_formats_pdu_to_json() {
    final var actual = X2X3Controller.formatPdu(pdu, "json");
    assertThat(actual).isEqualTo(
      """
      {"majorVersion":0,"minorVersion":5,"pduType":"X3_PDU","payloadFormat":"RTP","payloadDirection":"SENT_FROM_TARGET","xid":"62cd13da-7797-468c-befd-77f05008c996","correlationID":"AAECAwQFBgc=","conditionalAttributeFields":[],"payload":"AgMFBwsNERMXHR8lKSsvNQ=="}
      """.trim()
    );
  }

  @Test
  void it_formats_pdu_to_hex() {
    final var actual = X2X3Controller.formatPdu(pdu, "hex");
    assertThat(actual).isEqualTo(
      """
      00050002 00000028 00000010 00080003 62cd13da 7797468c befd77f0 5008c996 00010203 04050607 02030507 0b0d1113 171d1f25 292b2f35
      """.trim()
    );
  }
}
