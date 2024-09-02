package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sipgate.li.simulator.controller.response.ErrorResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.etsi.uri._03221.x1._2017._10.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SimulatorClientExtension.class)
@Tag("E2E")
class TaskControllerE2ETest {

  public static final String E164NUMBER = "4915799912345";

  private static TaskActivatedResponse createTask(final SimulatorClient client)
    throws IOException, InterruptedException {
    return client.post(
      "/task",
      Map.of(
        "e164number",
        E164NUMBER,
        "destinationId",
        "pre-shared-did",
        "xId",
        "55b848ea-b4c2-4d80-a4c9-46592792e5b7"
      ),
      TaskActivatedResponse.class
    );
  }

  @Nested
  class Test0_Started {

    @Test
    void testStartedState_getTaskDetails_unsuccessful(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.get("/task?xId=" + UUID.randomUUID().toString(), ErrorResponse.class, 502);
    }

    @Test
    void testStartedState_modifyTask_unsuccessful(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post(
        "/updateTask",
        Map.of("xId", UUID.randomUUID().toString(), "destinationId", "some-destination-id", "e164number", "some-e164"),
        ErrorResponse.class,
        502
      );
    }

    @Test
    void testStartedState_deactivateTask_unsuccessful(final SimulatorClient client)
      throws IOException, InterruptedException {
      client.post("/deleteTask", Map.of("xId", UUID.randomUUID().toString()), ErrorResponse.class, 502);
    }
  }

  @Nested
  class Test1_JustCreate {

    @Test
    void testJustCreate(final SimulatorClient client) throws IOException, InterruptedException {
      // WHEN
      final var response = createTask(client);

      // THEN
      assertThat(response.activateTaskResponse().getOK()).isEqualTo(OK.ACKNOWLEDGED_AND_COMPLETED);
      assertThat(response.xId()).matches("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
    }

    @Test
    void itGetsTaskDetails(final SimulatorClient client) throws IOException, InterruptedException {
      // GIVEN
      final var createResponse = createTask(client);

      // WHEN
      final var getResponse = client.get("/task?xId=" + createResponse.xId(), GetTaskDetailsResponse.class, 200);

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
    }
  }

  @Test
  void itReturns500ToErrorsOnCreate(final SimulatorClient client) throws IOException, InterruptedException {
    // WHEN
    final var response = client.post(
      "/task",
      Map.of("e164number", "112", "destinationId", "my-destination-id-123", "xId", UUID.randomUUID().toString()),
      ErrorResponse.class,
      502
    );

    // THEN
    assertThat(response.error().getAdmfIdentifier()).isNotEmpty();
    assertThat(response.error().getNeIdentifier()).isNotEmpty();
    assertThat(response.error().getMessageTimestamp()).isNotNull();
    assertThat(response.error().getVersion()).isNotEmpty();
  }

  @Nested
  class Test2_WithModify {

    @Test
    void itModifiedTask(final SimulatorClient client) throws IOException, InterruptedException {
      // GIVEN
      final var createResponse = createTask(client);

      // WHEN
      final var newE164 = "416701234";
      assertThat(newE164).isNotEqualTo(E164NUMBER);

      client.post(
        "/updateTask",
        Map.of("xId", createResponse.xId(), "e164number", newE164, "destinationId", "pre-shared-did"),
        ModifyTaskResponse.class,
        200
      );

      // THEN
      final var getResponse = client.get("/task?xId=" + createResponse.xId(), GetTaskDetailsResponse.class, 200);
      assertThat(
        getResponse
          .getTaskResponseDetails()
          .getTaskDetails()
          .getTargetIdentifiers()
          .getTargetIdentifier()
          .getFirst()
          .getE164Number()
      ).isEqualTo(newE164);
    }
  }

  @Nested
  class Test3_DuplicateXId {

    @Test
    void itBreaksOnDuplicateTasks(final SimulatorClient client) throws IOException, InterruptedException {
      // GIVEN
      final var taskResponse = createTask(client);

      // WHEN
      client.post(
        "/task",
        Map.of("e164number", E164NUMBER, "destinationId", "pre-shared-did", "xId", taskResponse.xId()),
        ErrorResponse.class,
        502
      );
    }
  }

  @Nested
  class Test4_Delete {

    @Test
    void itDeletesTask(final SimulatorClient client) throws IOException, InterruptedException {
      // GIVEN
      final var taskResponse = createTask(client);

      // WHEN
      client.post("/deleteTask", Map.of("xId", taskResponse.xId()), DeactivateTaskResponse.class, 200);

      // THEN
      client.get("/task?xId=" + taskResponse.xId(), ErrorResponse.class, 502);
    }
  }
}
