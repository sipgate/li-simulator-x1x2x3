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

  public static final String D_ID = "4faa0058-25ec-42c9-a945-4c3c8e0c7f8d";
  public static final String X_ID = "55b848ea-b4c2-4d80-a4c9-46592792e5b7";
  private static final String FRIENDLY_NAME = "we-are-friendly.example.com";
  private static final String FRIENDLY_NAME_MODIFIED = "they-are-unfriendly.example.com";
  private static final Map<String, String> DESTINATION_DETAILS = Map.of(
    "dId",
    D_ID,
    "friendlyName",
    FRIENDLY_NAME,
    "deliveryType",
    X_2_AND_X_3.name(),
    "tcpPort",
    "42069",
    "ipAddress",
    "host.docker.internal"
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

  final SimulatorClient client;

  private static void runAndIgnoreExceptions(final String prefix, final Callable<Object> f) {
    try {
      f.call();
    } catch (final Exception e) {
      LOGGER.trace("{}, ignoring", prefix, e);
    }
  }

  public E2ETest(final SimulatorClient client) {
    this.client = client;
  }

  @Nested
  class Started implements StatefulTest {

    @Override
    public void cleanup() {
      runAndIgnoreExceptions("remove task", () -> client.delete("/task/" + X_ID, DeactivateTaskResponse.class));

      runAndIgnoreExceptions("remove destination", () ->
        client.delete("/destination/" + D_ID, RemoveDestinationResponse.class)
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

    @Override
    public void init() {}

    @Test
    void it_creates_destination() throws IOException, InterruptedException {
      final var resp = client.post("/destination", DESTINATION_DETAILS, CreateDestinationResponse.class, 200);

      assertThat(resp.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_cant_find_unknown_destination() throws IOException, InterruptedException {
      client.get("/destination/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_cant_update_unknown_destination() throws IOException, InterruptedException {
      client.post("/destination/" + UUID.randomUUID(), DESTINATION_DETAILS, ErrorResponse.class, 502);
    }

    @Test
    void it_cant_delete_unknown_destination() throws IOException, InterruptedException {
      client.delete("/destination/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_cant_create_task_with_unknown_destination() throws IOException, InterruptedException {
      client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void list_all_details_is_empty() throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).doesNotContain(D_ID);
      assertThat(resp.tasks()).doesNotContain(X_ID);
    }
  }

  @Nested
  class DestinationAdded implements StatefulTest {

    private final Started started = new Started();

    @Override
    public void cleanup() {
      started.cleanup();
    }

    @Override
    public void init() throws Exception {
      started.init();
      started.it_creates_destination();
    }

    @Test
    void it_fails_when_destination_already_exists() throws IOException, InterruptedException {
      client.post("/destination", DESTINATION_DETAILS, ErrorResponse.class, 502);
    }

    @Test
    void it_contains_destination_details() throws IOException, InterruptedException {
      final var resp = client.get("/destination/" + D_ID, GetDestinationDetailsResponse.class, 200);

      final var details = resp.getDestinationResponseDetails().getDestinationDetails();

      assertThat(details.getDId()).isEqualTo(D_ID);
      assertThat(details.getFriendlyName()).isEqualTo(FRIENDLY_NAME);
    }

    @Test
    void list_all_details_contains_did() throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).contains(D_ID);
      assertThat(resp.tasks()).doesNotContain(X_ID);
    }

    @Test
    void it_modifies_destination() throws IOException, InterruptedException {
      final var response = client.post(
        "/destination/" + D_ID,
        MODIFIED_DESTINATION_DETAILS,
        ModifyDestinationResponse.class,
        200
      );
      assertThat(response.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_deletes_destination() throws IOException, InterruptedException {
      final var response = client.delete("/destination/" + D_ID, RemoveDestinationResponse.class);
      assertThat(response.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_fails_to_get_unknown_task() throws IOException, InterruptedException {
      client.get("/task/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_fails_to_modify_unknown_task() throws IOException, InterruptedException {
      client.post(
        "/task/" + UUID.randomUUID(),
        Map.of("destinationId", "some-destination-id", "e164number", "some-e164", "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void it_fails_to_deactivate_unknown_task() throws IOException, InterruptedException {
      client.delete("/task/" + UUID.randomUUID(), ErrorResponse.class, 502);
    }

    @Test
    void it_creates_task() throws IOException, InterruptedException {
      final var response = client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_AND_X_3.name()),
        TaskActivatedResponse.class,
        200
      );

      assertThat(response.activateTaskResponse().getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
      assertThat(response.xId()).isEqualTo(X_ID);
    }

    @Test
    void it_fails_to_activate_task_with_mismatching_delivery_type() throws IOException, InterruptedException {
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
  public class TaskAdded implements StatefulTest {

    private final DestinationAdded destinationAdded = new DestinationAdded();

    @Override
    public void cleanup() throws Exception {
      destinationAdded.cleanup();
    }

    @Override
    public void init() throws Exception {
      destinationAdded.init();
      destinationAdded.it_creates_task();
    }

    @Test
    void it_gets_task_details() throws IOException, InterruptedException {
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
    void list_all_details_contains_xid_and_did() throws IOException, InterruptedException {
      final var resp = client.get("/index", IndexController.IndexResponse.class, 200);

      assertThat(resp.destinations()).contains(D_ID);
      assertThat(resp.tasks()).contains(X_ID);
    }

    @Test
    void it_fails_to_create_task_with_duplicate_xId() throws IOException, InterruptedException {
      client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void it_deletes_task() throws IOException, InterruptedException {
      // WHEN
      client.delete("/task/" + X_ID, DeactivateTaskResponse.class);

      // THEN
      destinationAdded.it_fails_to_get_unknown_task();
    }

    @Test
    void it_fails_to_remove_destination_with_depending_task() throws IOException, InterruptedException {
      client.delete("/destination/" + D_ID, ErrorResponse.class, 502);
    }

    @Test
    void it_modifies_a_task() throws IOException, InterruptedException {
      client.post(
        "/task/" + X_ID,
        Map.of("e164number", E164NUMBER_MODIFIED, "destinationId", D_ID, "deliveryType", X_2_ONLY.name()),
        ModifyTaskResponse.class,
        200
      );
    }

    @Test
    void it_fails_to_modify_task_with_mismatching_delivery_type() throws IOException, InterruptedException {
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
  class DestinationModified implements StatefulTest {

    private final DestinationAdded destinationAdded = new DestinationAdded();

    @Override
    public void cleanup() throws Exception {
      destinationAdded.cleanup();
    }

    @Override
    public void init() throws Exception {
      destinationAdded.init();
      destinationAdded.it_modifies_destination();
    }

    @Test
    void it_gets_modified_data() throws IOException, InterruptedException {
      final var resp = client.get("/destination/" + D_ID, GetDestinationDetailsResponse.class, 200);

      final var details = resp.getDestinationResponseDetails().getDestinationDetails();

      assertThat(details.getDId()).isEqualTo(D_ID);
      assertThat(details.getFriendlyName()).isEqualTo(FRIENDLY_NAME_MODIFIED);
    }
  }

  @Nested
  class TaskModified implements StatefulTest {

    private final TaskAdded taskAdded = new TaskAdded();

    @Override
    public void cleanup() throws Exception {
      taskAdded.cleanup();
    }

    @Override
    public void init() throws Exception {
      taskAdded.init();
      taskAdded.it_modifies_a_task();
    }

    @Test
    void it_gets_modified_data() throws IOException, InterruptedException {
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
