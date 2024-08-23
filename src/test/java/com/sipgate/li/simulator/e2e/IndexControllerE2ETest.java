package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.simulator.controller.IndexController.IndexResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
class IndexControllerE2ETest {

  @Test
  void itReturns200ToRequests(final SimulatorClient client)
    throws IOException, InterruptedException {
    // WHEN
    final var response = client.get("/index", IndexResponse.class);

    // THEN
    assertThat(response.tasks()).isNotNull();
  }
}
