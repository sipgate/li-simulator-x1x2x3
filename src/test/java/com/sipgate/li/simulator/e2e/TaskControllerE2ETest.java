package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TaskControllerE2ETest extends E2eTestCase {

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
    assertThat(responseBody.get("xId").asText()).matches(
      "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
    );
  }

  @Test
  void itReturns500ToErrors() throws IOException, InterruptedException {
    // WHEN
    final var response = postUnauthenticated(
      "/task",
      Map.of(
        "e164number",
        "112",
        "destinationId",
        "my-destination-id-123"
      )
    );

    // THEN
    assertThat(response.statusCode()).isEqualTo(500);

    final var responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody.get("error").has("admfIdentifier")).isTrue();
    assertThat(responseBody.get("error").has("neIdentifier")).isTrue();
    assertThat(responseBody.get("error").has("messageTimestamp")).isTrue();
    assertThat(responseBody.get("error").has("version")).isTrue();
  }
}
