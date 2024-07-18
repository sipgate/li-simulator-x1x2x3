package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ListAllDetailsControllerE2eTest extends E2eTestCase {

  @Test
  void itReturns200ToRequests() throws IOException, InterruptedException {
    // WHEN
    final var response = getUnauthenticated("/listAllDetails");

    // THEN
    assertThat(response.statusCode()).isEqualTo(200);

    final var responseBody = objectMapper.readTree(response.body());
    assertThat(responseBody.get("tasks").isArray()).isTrue();
    assertThat(responseBody.get("tasks")).hasSize(2);
    assertThat(responseBody.get("tasks").get(0).asText()).isEqualTo(
      "29f28e1c-f230-486a-a860-f5a784ab9172"
    );
    assertThat(responseBody.get("tasks").get(1).asText()).isEqualTo(
      "4f79fd4a-b237-48c3-b5d3-334ed9191f33"
    );

    assertThat(responseBody.get("destinations").isArray()).isTrue();
    assertThat(responseBody.get("destinations")).hasSize(2);
    assertThat(responseBody.get("destinations").get(0).asText()).isEqualTo(
      "b0ce308c-aa17-42bd-a27b-287bcb5b3468"
    );
    assertThat(responseBody.get("destinations").get(1).asText()).isEqualTo(
      "6d44e794-707a-4338-8c43-15f4213f508a"
    );
  }
}
