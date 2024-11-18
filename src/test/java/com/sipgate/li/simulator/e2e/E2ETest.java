package com.sipgate.li.simulator.e2e;

import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.DESTINATION_IN_USE;
import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.DID_ALREADY_EXISTS;
import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.DID_DOES_NOT_EXIST;
import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.INVALID_COMBINATION_OF_DELIVERYTYPE_AND_DESTINATIONS;
import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.XID_ALREADY_EXISTS;
import static com.sipgate.li.lib.x1.protocol.error.ErrorResponseException.XID_DOES_NOT_EXIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.etsi.uri._03221.x1._2017._10.DeliveryType.X_2_AND_X_3;
import static org.etsi.uri._03221.x1._2017._10.DeliveryType.X_2_ONLY;
import static org.etsi.uri._03221.x1._2017._10.DeliveryType.X_3_ONLY;

import com.sipgate.li.simulator.controller.IndexController;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.etsi.uri._03221.x1._2017._10.ActivateTaskResponse;
import org.etsi.uri._03221.x1._2017._10.CreateDestinationResponse;
import org.etsi.uri._03221.x1._2017._10.DeactivateTaskResponse;
import org.etsi.uri._03221.x1._2017._10.ErrorResponse;
import org.etsi.uri._03221.x1._2017._10.GetDestinationDetailsResponse;
import org.etsi.uri._03221.x1._2017._10.GetTaskDetailsResponse;
import org.etsi.uri._03221.x1._2017._10.ModifyDestinationResponse;
import org.etsi.uri._03221.x1._2017._10.ModifyTaskResponse;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.etsi.uri._03221.x1._2017._10.RemoveDestinationResponse;
import org.etsi.uri._03221.x1._2017._10.RequestMessageType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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
    X_2_AND_X_3.name(),
    "tcpPort",
    "12345",
    "ipAddress",
    "192.0.2.23"
  );
  //These have to be non german tel. numbers, so litc acks them and doesn't fill up the dlq
  public static final String E164NUMBER = "2125552368"; //GHOSTBUSTERS
  public static final String E164NUMBER_MODIFIED = "2125552323"; //CALL GOD
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

  @BeforeAll
  static void beforeAll() {
    runAndIgnoreExceptions("reload wiremock mappings", () -> {
      final var wiremockHost = System.getProperty("wiremockHost", "localhost");
      final var wiremockPort = Integer.parseInt(System.getProperty("wiremockPort", "8082"));
      try (final var wireMock = HttpClient.newHttpClient()) {
        return wireMock.send(
          HttpRequest.newBuilder()
            .uri(URI.create(String.format("http://%s:%d/__admin/mappings/reset", wiremockHost, wiremockPort)))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build(),
          HttpResponse.BodyHandlers.discarding()
        );
      }
    });
  }

  @Nested
  class Started implements StatefulTest {

    @Override
    public void cleanup() {
      runAndIgnoreExceptions("remove task", () -> client.delete("/task/" + X_ID, DeactivateTaskResponse.class));

      runAndIgnoreExceptions("remove destination", () ->
        client.delete("/destination/" + D_ID, RemoveDestinationResponse.class)
      );

      runAndIgnoreExceptions("reset wiremock scenarios", () -> {
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
      // GIVEN
      final var dID = UUID.fromString("00000000-0000-1337-0000-000000000152").toString();

      // WHEN
      final var response = client.get("/destination/" + dID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.GET_DESTINATION_DETAILS, DID_DOES_NOT_EXIST, dID);
    }

    @Test
    void it_cant_update_unknown_destination() throws IOException, InterruptedException {
      // GIVEN
      final var dID = UUID.fromString("00000000-0000-1337-0000-000000000164").toString();

      // WHEN
      final var response = client.post("/destination/" + dID, DESTINATION_DETAILS, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.MODIFY_DESTINATION, DID_DOES_NOT_EXIST, dID);
    }

    @Test
    void it_cant_delete_unknown_destination() throws IOException, InterruptedException {
      // GIVEN
      final var dID = UUID.fromString("00000000-0000-1337-0000-000000000001").toString();

      // WHEN
      final var response = client.delete("/destination/" + dID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.REMOVE_DESTINATION, DID_DOES_NOT_EXIST, dID);
    }

    @Test
    void it_cant_create_task_with_unknown_destination() throws IOException, InterruptedException {
      // WHEN
      final var response = client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        400
      );

      // THEN
      assertErrorResponse(response, RequestMessageType.ACTIVATE_TASK, DID_DOES_NOT_EXIST, D_ID);
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
      // WHEN
      final var response = client.post("/destination", DESTINATION_DETAILS, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.CREATE_DESTINATION, DID_ALREADY_EXISTS, D_ID);
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
      // GIVEN
      final var xID = UUID.randomUUID().toString();

      // WHEN
      final var response = client.get("/task/" + xID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.GET_TASK_DETAILS, XID_DOES_NOT_EXIST, xID);
    }

    @Test
    void it_fails_to_modify_unknown_task() throws IOException, InterruptedException {
      // GIVEN
      final var xID = UUID.randomUUID().toString();

      // WHEN
      final var response = client.post(
        "/task/" + xID,
        Map.of("destinationId", D_ID, "e164number", "some-e164", "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        400
      );

      // THEN
      assertErrorResponse(response, RequestMessageType.MODIFY_TASK, XID_DOES_NOT_EXIST, xID);
    }

    @Test
    void it_fails_to_deactivate_unknown_task() throws IOException, InterruptedException {
      // GIVEN
      final var xID = UUID.randomUUID().toString();

      // WHEN
      final var response = client.delete("/task/" + xID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.DEACTIVATE_TASK, XID_DOES_NOT_EXIST, xID);
    }

    @Test
    void it_creates_task() throws IOException, InterruptedException {
      final var response = client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_AND_X_3.name()),
        ActivateTaskResponse.class,
        200
      );

      assertThat(response.getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
    }

    @Test
    void it_fails_to_activate_task_with_mismatching_delivery_type() throws IOException, InterruptedException {
      //GIVEN: Modify Destination to mismatch new tasks delivery type
      final Map<String, String> DESTINATION_DETAILS_X2_ONLY = new HashMap<>(DESTINATION_DETAILS);
      DESTINATION_DETAILS_X2_ONLY.put("deliveryType", X_2_ONLY.name());
      DESTINATION_DETAILS_X2_ONLY.put("friendlyName", "special snowflake"); //Used in wiremock for special cases
      client.post("/destination/" + D_ID, DESTINATION_DETAILS_X2_ONLY, ModifyDestinationResponse.class, 200);
      //WHEN
      final var mismatchingDeliveryType = X_3_ONLY;
      assertThat(DESTINATION_DETAILS.get("deliveryType")).isNotIn(X_2_AND_X_3, mismatchingDeliveryType);

      //THEN
      final var response = client.post(
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
        400
      );

      assertErrorResponse(
        response,
        RequestMessageType.ACTIVATE_TASK,
        INVALID_COMBINATION_OF_DELIVERYTYPE_AND_DESTINATIONS,
        null
      );
    }
  }

  @Nested
  public class TaskAdded implements StatefulTest {

    private final DestinationAdded destinationAdded = new DestinationAdded();

    @Override
    public void cleanup() {
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
      final var response = client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", D_ID, "xId", X_ID, "deliveryType", X_2_ONLY.name()),
        ErrorResponse.class,
        400
      );

      assertErrorResponse(response, RequestMessageType.ACTIVATE_TASK, XID_ALREADY_EXISTS, X_ID);
    }

    @Test
    void it_deletes_task() throws IOException, InterruptedException {
      // WHEN
      client.delete("/task/" + X_ID, DeactivateTaskResponse.class);

      // THEN
      final var response = client.get("/task/" + X_ID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.GET_TASK_DETAILS, XID_DOES_NOT_EXIST, X_ID);
    }

    @Test
    void it_fails_to_remove_destination_with_depending_task() throws IOException, InterruptedException {
      // WHEN
      final var response = client.delete("/destination/" + D_ID, ErrorResponse.class, 400);

      // THEN
      assertErrorResponse(response, RequestMessageType.GET_TASK_DETAILS, DESTINATION_IN_USE, X_ID);
    }

    @Test
    void it_modifies_a_task() throws IOException, InterruptedException {
      client.post(
        "/task/" + X_ID,
        Map.of("e164number", E164NUMBER_MODIFIED, "destinationId", D_ID, "deliveryType", X_2_AND_X_3.name()),
        ModifyTaskResponse.class,
        200
      );
    }

    @Test
    void it_fails_to_modify_task_with_mismatching_delivery_type() throws IOException, InterruptedException {
      //GIVEN: Modify Destination to mismatch new tasks delivery type
      final Map<String, String> DESTINATION_DETAILS_X2_ONLY = new HashMap<>(DESTINATION_DETAILS);
      DESTINATION_DETAILS_X2_ONLY.put("deliveryType", X_2_ONLY.name());
      DESTINATION_DETAILS_X2_ONLY.put("friendlyName", "special snowflake"); //Used in wiremock for special matching
      client.post("/destination/" + D_ID, DESTINATION_DETAILS_X2_ONLY, ModifyDestinationResponse.class, 200);
      //WHEN
      final var mismatchingDeliveryType = X_3_ONLY;
      assertThat(DESTINATION_DETAILS.get("deliveryType")).isNotIn(X_2_AND_X_3, mismatchingDeliveryType);
      //THEN
      final var response = client.post(
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
        400
      );

      assertErrorResponse(
        response,
        RequestMessageType.MODIFY_TASK,
        INVALID_COMBINATION_OF_DELIVERYTYPE_AND_DESTINATIONS,
        null
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

  private static void assertErrorResponse(
    final ErrorResponse response,
    final RequestMessageType type,
    final BigInteger code,
    final String description
  ) {
    assertThat(response.getRequestMessageType()).isEqualTo(type);
    assertThat(response.getErrorInformation().getErrorCode()).isEqualTo(code);
    assertThat(response.getErrorInformation().getErrorDescription()).isEqualTo(description);
  }
}
