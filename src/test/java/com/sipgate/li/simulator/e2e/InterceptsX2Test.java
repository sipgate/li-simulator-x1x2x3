package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.lib.x2x3.*;
import io.netty.buffer.Unpooled;
import java.util.Base64;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag("E2E")
@ExtendWith(SimulatorClientExtension.class)
class InterceptsX2Test {

  @Test
  void simulator_receives_x2(final SimulatorClient simulatorClient) throws Exception {
    // GIVEN
    final String body = "INVITE sip:someone@example.org\n";

    // WHEN
    simulatorClient.post("/sip", "application/octet-stream", body, Void.class, 204);

    // THEN
    final var base64edByteStreamOfAPduObject = simulatorClient.get("/x2/last", String.class);
    final var decoded = Unpooled.wrappedBuffer(Base64.getDecoder().decode(base64edByteStreamOfAPduObject));

    final PduObject result = new X2X3Decoder(Integer.MAX_VALUE, Integer.MAX_VALUE).decode(decoded).get();
    assertThat(result).isNotNull();
    assertThat(result.pduType()).isEqualTo(PduType.X2_PDU);
    assertThat(result.payloadFormat()).isEqualTo(PayloadFormat.SIP);
    assertThat(result.payloadDirection()).isEqualTo(PayloadDirection.SENT_FROM_TARGET);
    assertThat(result.correlationID()).isEqualTo(new byte[8]);
    assertThat(result.xid()).isNotNull();
    assertThat(result.payload()).isEqualTo(body.getBytes());
  }
}
