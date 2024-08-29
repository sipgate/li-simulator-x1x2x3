package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.sipgate.li.simulator.controller.response.ErrorResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
@Tag("E2E")
class TaskControllerE2ETest {

  @Test
  void itReturns200ToRequests(final SimulatorClient client)
    throws IOException, InterruptedException {
    // WHEN
    final var response = client.post(
      "/task",
      Map.of(
        "e164number",
        "4915799912345",
        "destinationId",
        "pre-shared-did",
        "xId",
        UUID.randomUUID().toString()
      ),
      TaskActivatedResponse.class
    );

    // THEN

    assertThat(response.activateTaskResponse().getOK()).isEqualTo(
      OK.ACKNOWLEDGED_AND_COMPLETED
    );
    assertThat(response.xId()).matches(
      "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
    );
  }

  @Test
  void itReturns500ToErrors(final SimulatorClient client)
    throws IOException, InterruptedException {
    // WHEN
    final var response = client.post(
      "/task",
      Map.of(
        "e164number",
        "112",
        "destinationId",
        "my-destination-id-123",
        "xId",
        UUID.randomUUID().toString()
      ),
      ErrorResponse.class,
      500
    );

    // THEN
    assertThat(response.error().getAdmfIdentifier()).isNotEmpty();
    assertThat(response.error().getNeIdentifier()).isNotEmpty();
    assertThat(response.error().getMessageTimestamp()).isNotNull();
    assertThat(response.error().getVersion()).isNotEmpty();
  }
}
