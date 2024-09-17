package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.etsi.uri._03221.x1._2017._10.DeliveryType.*;

import com.sipgate.li.simulator.controller.IndexController;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.etsi.uri._03221.x1._2017._10.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("E2E")
@ExtendWith(SimulatorClientExtension.class)
public class E2ETest {

  private static final String D_ID = "4faa0058-25ec-42c9-a945-4c3c8e0c7f8d";
  private static final String X_ID = "55b848ea-b4c2-4d80-a4c9-46592792e5b7";
  private static final String FRIENDLY_NAME = "we-are-friendly.example.com";
  private static final String FRIENDLY_NAME_MODIFIED = "they-are-unfriendly.example.com";
  private static final Map<String, String> DESTINATION_DETAILS = Map.of(
    "dId",
    D_ID,
    "friendlyName",
    FRIENDLY_NAME,
    "deliveryType",
    X_2_ONLY.name(),
    "tcpPort",
    "12345",
    "ipAddress",
    "192.0.2.23"
  );

  private static final Map<String, String> MODIFIED_DESTINATION_DETAILS = Map.of(
    "dId",
    D_ID,
    "friendlyName",
    FRIENDLY_NAME_MODIFIED,
    "deliveryType",
    X_2_ONLY.name(),
    "tcpPort",
    "12345",
    "ipAddress",
    "192.0.2.23"
  );

  public static final String E164NUMBER = "4915799912345";
  public static final String E164NUMBER_MODIFIED = "4915799912346";
  public static final Logger LOGGER = LoggerFactory.getLogger(E2ETest.class);

  private static void runAndIgnoreExceptions(final String prefix, final Callable<Object> f) {
    try {
      f.call();
    } catch (final Exception e) {
      LOGGER.error("{}, ignoring", prefix, e);
    }
  }

  @Nested
  class Started {

    @BeforeEach
    void setupState(final SimulatorClient client) {
      runAndIgnoreExceptions("remove task", () -> client.post("/task/remove/" + X_ID, DeactivateTaskResponse.class));

      runAndIgnoreExceptions("remove destination", () ->
        client.post("/destination/remove/" + D_ID, RemoveDestinationResponse.class)
      );

      runAndIgnoreExceptions("reset wiremock", () -> {
        final var wiremockHost = System.getProperty("wiremockHost", "localhost");
        final var wiremockPort = Integer.parseInt(System.getProperty("wiremockPort", "8082"));
        try (final var wireMock = HttpClient.newHttpClient()) {
          return wireMock.send(
            HttpRequest.newBuilder()
              .uri(URI.create(String.format("http://%s:%d/__admin/scenarios/reset", wiremockHost, wiremockPort)))
              .POST(HttpRequest.BodyPublishers.noBody())
              .build(),
            HttpResponse.BodyHandlers.discarding()
          );
        }
      });
    }

    @Test
    void it_creates_destination(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.post("/destination", DESTINATION_DETAILS, CreateDestinationResponse.class, 200);

      assertThat(resp.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_cant_find_unknown_destination(final SimulatorClient client) throws IOException, InterruptedException {
      client.get("/destination/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_cant_update_unknown_destination(final SimulatorClient client) throws IOException, InterruptedException {
      client.post("/destination/" + UUID.randomUUID(), DESTINATION_DETAILS, ErrorResponse.class, 502);
    }

    @Test
    void it_cant_delete_unknown_destination(final SimulatorClient client) throws IOException, InterruptedException {
      client.post("/destination/remove/" + UUID.randomUUID(), DESTINATION_DETAILS, ErrorResponse.class, 502);
    }

    @Test
    void it_cant_create_task_with_unknown_destination(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void list_all_details_is_empty(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).doesNotContain(D_ID);
      assertThat(resp.tasks()).doesNotContain(X_ID);
    }
  }

  @Nested
  class DestinationAdded {

    @BeforeEach
    void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new Started().setupState(client);
      new Started().it_creates_destination(client);
    }

    @Test
    void it_fails_when_destination_already_exists(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post("/destination", DESTINATION_DETAILS, ErrorResponse.class, 502);
    }

    @Test
    void it_contains_destination_details(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.get("/destination/" + D_ID, GetDestinationDetailsResponse.class, 200);

      final var details = resp.getDestinationResponseDetails().getDestinationDetails();

      assertThat(details.getDId()).isEqualTo(D_ID);
      assertThat(details.getFriendlyName()).isEqualTo(FRIENDLY_NAME);
    }

    @Test
    void list_all_details_contains_did(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).contains(D_ID);
      assertThat(resp.tasks()).doesNotContain(X_ID);
    }

    @Test
    void it_modifies_destination(final SimulatorClient client) throws IOException, InterruptedException {
      final var response = client.post(
        "/destination/" + D_ID,
        MODIFIED_DESTINATION_DETAILS,
        ModifyDestinationResponse.class,
        200
      );
      assertThat(response.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_deletes_destination(final SimulatorClient client) throws IOException, InterruptedException {
      final var response = client.post(
        "/destination/remove/" + D_ID,
        DESTINATION_DETAILS,
        RemoveDestinationResponse.class,
        200
      );
      assertThat(response.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_fails_to_get_unknown_task(final SimulatorClient client) throws IOException, InterruptedException {
      client.get("/task/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_fails_to_modify_unknown_task(final SimulatorClient client) throws IOException, InterruptedException {
      client.post(
        "/task/" + UUID.randomUUID(),
        Map.of("destinationId", "some-destination-id", "e164number", "some-e164", "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void it_fails_to_deactivate_unknown_task(final SimulatorClient client) throws IOException, InterruptedException {
      client.post("/task/remove/" + UUID.randomUUID(), Map.of(), ErrorResponse.class, 502);
    }

    @Test
    void it_creates_task(final SimulatorClient client) throws IOException, InterruptedException {
      final var response = client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        TaskActivatedResponse.class,
        200
      );

      assertThat(response.activateTaskResponse().getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
      assertThat(response.xId()).isEqualTo(X_ID);
    }

    @Test
    void it_fails_to_activate_task_with_mismatching_delivery_type(final SimulatorClient client)
      throws IOException, InterruptedException {
      final var mismatchingDeliveryType = X_3_ONLY;
      assertThat(DESTINATION_DETAILS.get("deliveryType")).isNotIn(X_2_AND_X_3, mismatchingDeliveryType);

      client.post(
        "/task",
        Map.of(
          "e164number",
          E164NUMBER,
          "destinationId",
          D_ID,
          "xId",
          X_ID,
          "deliveryType",
          mismatchingDeliveryType.name()
        ),
        ErrorResponse.class,
        502
      );
    }
  }

  @Nested
  public class TaskAdded {

    @BeforeEach
    public void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new DestinationAdded().setupState(client);
      new DestinationAdded().it_creates_task(client);
    }

    @Test
    void it_gets_task_details(final SimulatorClient client) throws IOException, InterruptedException {
      // WHEN
      final var getResponse = client.get("/task/" + X_ID, GetTaskDetailsResponse.class, 200);

      // THEN
      assertThat(
        getResponse
          .getTaskResponseDetails()
          .getTaskDetails()
          .getTargetIdentifiers()
          .getTargetIdentifier()
          .getFirst()
          .getE164Number()
      ).isEqualTo(E164NUMBER);

      assertThat(getResponse.getTaskResponseDetails().getTaskDetails().getListOfDIDs().getDId().getFirst()).isEqualTo(
        D_ID
      );
    }

    @Test
    void list_all_details_contains_xid_and_did(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).contains(D_ID);
      assertThat(resp.tasks()).contains(X_ID);
    }

    @Test
    void it_fails_to_create_task_with_duplicate_xId(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void it_deletes_task(final SimulatorClient client) throws IOException, InterruptedException {
      // WHEN
      client.post("/task/remove/" + X_ID, Map.of(), DeactivateTaskResponse.class, 200);

      // THEN
      new DestinationAdded().it_fails_to_get_unknown_task(client);
    }

    @Test
    void it_fails_to_remove_destination_with_depending_task(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post("/destination/remove/" + D_ID, Map.of(), ErrorResponse.class, 502);
    }

    @Test
    void it_modifies_a_task(final SimulatorClient client) throws IOException, InterruptedException {
      client.post(
        "/task/" + X_ID,
        Map.of("e164number", E164NUMBER_MODIFIED, "destinationId", D_ID, "deliveryType", X_2_ONLY.name()),
        ModifyTaskResponse.class,
        200
      );
    }

    @Test
    void it_fails_to_modify_task_with_mismatching_delivery_type(final SimulatorClient client)
      throws IOException, InterruptedException {
      final var mismatchingDeliveryType = X_3_ONLY;
      assertThat(DESTINATION_DETAILS.get("deliveryType")).isNotIn(X_2_AND_X_3, mismatchingDeliveryType);

      client.post(
        "/task/" + X_ID,
        Map.of(
          "e164number",
          E164NUMBER,
          "destinationId",
          D_ID,
          "xId",
          X_ID,
          "deliveryType",
          mismatchingDeliveryType.name()
        ),
        ErrorResponse.class,
        502
      );
    }
  }

  @Nested
  class DestinationModified {

    @BeforeEach
    void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new DestinationAdded().setupState(client);
      new DestinationAdded().it_modifies_destination(client);
    }

    @Test
    void it_gets_modified_data(final SimulatorClient client) throws IOException, InterruptedException {
      final var resp = client.get("/destination/" + D_ID, GetDestinationDetailsResponse.class, 200);

      final var details = resp.getDestinationResponseDetails().getDestinationDetails();

      assertThat(details.getDId()).isEqualTo(D_ID);
      assertThat(details.getFriendlyName()).isEqualTo(FRIENDLY_NAME_MODIFIED);
    }
  }

  @Nested
  class TaskModified {

    @BeforeEach
    void setupState(final SimulatorClient client) throws IOException, InterruptedException {
      new TaskAdded().setupState(client);
      new TaskAdded().it_modifies_a_task(client);
    }

    @Test
    void it_gets_modified_data(final SimulatorClient client) throws IOException, InterruptedException {
      final var getResponse = client.get("/task/" + X_ID, GetTaskDetailsResponse.class, 200);
      assertThat(
        getResponse
          .getTaskResponseDetails()
          .getTaskDetails()
          .getTargetIdentifiers()
          .getTargetIdentifier()
          .getFirst()
          .getE164Number()
      ).isEqualTo(E164NUMBER_MODIFIED);
    }
  }
}
