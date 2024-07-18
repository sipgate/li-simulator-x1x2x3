package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ActivateTaskControllerE2eTest extends E2eTestCase {

  @Test
  void itReturns200ToRequests() throws IOException, InterruptedException {
    // WHEN
    final var response = postUnauthenticated(
      "/task",
      Map.of(
        "e164number",
        "4915799912345",
        "destinationId",
        "my-destination-id-123"
      )
    );

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final var responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody.get("ok").asText()).isEqualTo(
      "ACKNOWLEDGED_AND_COMPLETED"
    );
  }
}
