package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.etsi.uri._03221.x1._2017._10.DeliveryType.X_2_ONLY;

import com.sipgate.li.simulator.controller.response.ErrorResponse;
import java.io.IOException;
import java.util.Map;
import org.etsi.uri._03221.x1._2017._10.CreateDestinationResponse;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
public class E2ETest {

  private static final String D_ID = "4faa0058-25ec-42c9-a945-4c3c8e0c7f8d";
  private static final Map<String, String> DESTINATION_DETAILS = Map.of(
    "dId",
    D_ID,
    "friendlyName",
    "we-are-friendly.example.com",
    "deliveryType",
    X_2_ONLY.name(),
    "tcpPort",
    "12345",
    "ipAddress",
    "192.0.2.23"
  );

  @BeforeEach
  void setupState() {
    // TODO: Implement preventive delete of destination and task
  }

  @Nested
  class Started {

    @Test
    void it_creates_destination(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.post("/destination", DESTINATION_DETAILS, CreateDestinationResponse.class, 200);

      assertThat(resp.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }
  }

  @Nested
  class DestinationAdded {

    @BeforeEach
    void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new Started().it_creates_destination(client);
    }

    @Test
    void it_fails_when_destination_already_exists(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post("/destination", DESTINATION_DETAILS, ErrorResponse.class, 502);
    }
  }

  @Nested
  class TaskAdded {

    @BeforeEach
    void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new DestinationAdded().setupState(client);
    }
  }
}
