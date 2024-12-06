package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.client.X1Client;
import com.sipgate.li.lib.x1.client.X1ClientException;
import com.sipgate.li.lib.x1.client.X1RequestFactory;
import com.sipgate.li.simulator.controller.response.SimulatorErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.etsi.uri._03221.x1._2017._10.ActivateTaskRequest;
import org.etsi.uri._03221.x1._2017._10.ActivateTaskResponse;
import org.etsi.uri._03221.x1._2017._10.DeactivateTaskRequest;
import org.etsi.uri._03221.x1._2017._10.DeactivateTaskResponse;
import org.etsi.uri._03221.x1._2017._10.DeliveryType;
import org.etsi.uri._03221.x1._2017._10.ErrorResponse;
import org.etsi.uri._03221.x1._2017._10.GetTaskDetailsRequest;
import org.etsi.uri._03221.x1._2017._10.GetTaskDetailsResponse;
import org.etsi.uri._03221.x1._2017._10.ListOfDids;
import org.etsi.uri._03221.x1._2017._10.ListOfTargetIdentifiers;
import org.etsi.uri._03221.x1._2017._10.ModifyTaskRequest;
import org.etsi.uri._03221.x1._2017._10.ModifyTaskResponse;
import org.etsi.uri._03221.x1._2017._10.TargetIdentifier;
import org.etsi.uri._03221.x1._2017._10.TaskDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

  private final X1RequestFactory x1RequestFactory;
  private final X1Client x1Client;

  public TaskController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
    this.x1RequestFactory = x1RequestFactory;
    this.x1Client = x1Client;
  }

  @Operation(
    summary = "Get details",
    description = """
    Used by the ADMF to check the details of a running task. The details can include more information like the number of extracted bytes, the state of the interception, etc.
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Task is returned."),
      @ApiResponse(
        responseCode = "502",
        description = "The GetTaskDetails operation was not handled properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @GetMapping("/task/{xId}")
  public GetTaskDetailsResponse getTaskDetails(@PathVariable final String xId)
    throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.builder(GetTaskDetailsRequest.builder()).withXId(xId).build();

    return x1Client.request(req, GetTaskDetailsResponse.class);
  }

  @Operation(
    summary = "Activate Task",
    description = """
      Used by the ADMF to add a new Task to an NE
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Task was created."),
      @ApiResponse(
        responseCode = "502",
        description = "The TaskActivate operation was not handled properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/task")
  public ActivateTaskResponse activateTask(
    @RequestParam final String e164number,
    @RequestParam final String destinationId,
    @RequestParam final String xId,
    @RequestParam final DeliveryType deliveryType
  ) throws X1ClientException, InterruptedException {
    final var taskDetails = makeTaskDetails(e164number, destinationId, xId, deliveryType);

    final var req = x1RequestFactory.builder(ActivateTaskRequest.builder()).withTaskDetails(taskDetails).build();

    return x1Client.request(req, ActivateTaskResponse.class);
  }

  private static TaskDetails makeTaskDetails(
    final String e164number,
    final String destinationId,
    final String xId,
    final DeliveryType deliveryType
  ) {
    return TaskDetails.builder()
      .withXId(xId)
      .withTargetIdentifiers(
        ListOfTargetIdentifiers.builder()
          .addTargetIdentifier(TargetIdentifier.builder().withE164Number(e164number).build())
          .build()
      )
      .withDeliveryType(deliveryType)
      .withListOfDIDs(ListOfDids.builder().addDId(destinationId).build())
      .build();
  }

  @Operation(
    summary = "Update Task",
    description = """
    Used by the ADMF to update an existing Task.
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Task was updated."),
      @ApiResponse(
        responseCode = "502",
        description = "The TaskModify operation was not handled properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/task/{xId}")
  public ModifyTaskResponse updateTask(
    @PathVariable final String xId,
    @RequestParam final String e164number,
    @RequestParam final String destinationId,
    @RequestParam final DeliveryType deliveryType
  ) throws X1ClientException, InterruptedException {
    final var taskDetails = makeTaskDetails(e164number, destinationId, xId, deliveryType);
    final var req = x1RequestFactory.builder(ModifyTaskRequest.builder()).withTaskDetails(taskDetails).build();

    return x1Client.request(req, ModifyTaskResponse.class);
  }

  @Operation(
    summary = "Deactivate Task",
    description = """
      Used by the ADMF to remove an existing Task from the NE
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Task was removed."),
      @ApiResponse(
        responseCode = "502",
        description = "The TaskDeactivate operation was not handled properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @DeleteMapping("/task/{xId}")
  public DeactivateTaskResponse deactivateTask(@PathVariable final String xId)
    throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.builder(DeactivateTaskRequest.builder()).withXId(xId).build();

    return x1Client.request(req, DeactivateTaskResponse.class);
  }
}
