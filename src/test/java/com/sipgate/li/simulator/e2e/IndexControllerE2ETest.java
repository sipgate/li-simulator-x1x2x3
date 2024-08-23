package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.simulator.controller.IndexController.IndexResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
@Tag("E2E")
class IndexControllerE2ETest {

  @Test
  void itReturnsCreatedXId(final SimulatorClient client)
    throws IOException, InterruptedException {
    // GIVEN
    final var taskActivatedResponse = client.post(
      "/task",
      Map.of("e164number", "4915799912345", "destinationId", "pre-shared-did"),
      TaskActivatedResponse.class
    );

    // WHEN
    final var indexResponse = client.get("/index", IndexResponse.class);

    // THEN
    assertThat(indexResponse.tasks()).isNotNull();
    assertThat(indexResponse.tasks()).contains(taskActivatedResponse.xId());
  }
}
