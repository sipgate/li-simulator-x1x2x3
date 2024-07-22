package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConnectionControllerTest extends E2eTestCase {

  @ParameterizedTest
  @ValueSource(strings = { "/connection/ping", "/connection/keepalive" })
  void itReturns200ToRequests(final String path)
    throws IOException, InterruptedException {
    // WHEN
    final var response = postUnauthenticated(path);

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final var responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody.asText()).isEqualTo(
      "ACKNOWLEDGED_AND_COMPLETED"
    );
  }
}
