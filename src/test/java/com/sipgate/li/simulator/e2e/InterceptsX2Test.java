package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.lib.x2x3.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
class InterceptsX2Test {

  @Test
  void simulator_receives_x2(final SimulatorClient simulatorClient) throws Exception {
    // GIVEN
    final String body = "INVITE sip:someone@example.org\n";

    // WHEN
    simulatorClient.post("/sip", body, Void.class);

    // THEN
    final PduObject result = simulatorClient.get("/x2/last", PduObject.class);
    assertThat(result).isNotNull();
    assertThat(result.payload()).isEqualTo(body.getBytes());
  }
}
