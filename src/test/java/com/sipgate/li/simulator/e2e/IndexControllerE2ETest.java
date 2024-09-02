package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.simulator.controller.IndexController.IndexResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import java.io.IOException;
import java.util.Map;
import org.etsi.uri._03221.x1._2017._10.DeactivateTaskResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
@Tag("E2E")
class IndexControllerE2ETest {

  @Test
  void it_does_not_return_deactivated_tasks(final SimulatorClient client) throws IOException, InterruptedException {
    // GIVEN
    final var taskActivatedResponse = createTask(client, "50b93d1e-1b53-4d63-aacb-e4d99811bc0b");
    final var indexResponse = client.get("/index", IndexResponse.class);
    assertThat(indexResponse.tasks()).contains(taskActivatedResponse.xId());

    // WHEN
    client.post("/deleteTask", Map.of("xId", taskActivatedResponse.xId()), DeactivateTaskResponse.class);

    // THEN
    final var indexResponseAfterDelete = client.get("/index", IndexResponse.class);
    assertThat(indexResponseAfterDelete.tasks()).doesNotContain(taskActivatedResponse.xId());
  }

  private static TaskActivatedResponse createTask(final SimulatorClient client, final String xId)
    throws IOException, InterruptedException {
    return client.post(
      "/task",
      Map.of("e164number", "4915799912345", "destinationId", "pre-shared-did", "xId", xId),
      TaskActivatedResponse.class
    );
  }
}
