package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1ClientException;
import com.sipgate.li.lib.x1.X1RequestFactory;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import com.sipgate.li.simulator.controller.response.TaskActivatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.etsi.uri._03221.x1._2017._10.*;
import org.springframework.web.bind.annotation.*;

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
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @GetMapping("/task")
  public GetTaskDetailsResponse getTaskDetails(@RequestParam final String xId)
    throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.create(GetTaskDetailsRequest.class);
    req.setXId(xId);

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
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/task")
  public TaskActivatedResponse activateTask(
    @RequestParam final String e164number,
    @RequestParam final String destinationId,
    @RequestParam final String xId
  ) throws X1ClientException, InterruptedException {
    final var taskDetails = makeTaskDetails(e164number, destinationId, xId);

    final var req = x1RequestFactory.create(ActivateTaskRequest.class);
    req.setTaskDetails(taskDetails);

    final var resp = x1Client.request(req, ActivateTaskResponse.class);
    return new TaskActivatedResponse(resp, xId);
  }

  private static TaskDetails makeTaskDetails(final String e164number, final String destinationId, final String xId) {
    final var targetIdentifier = new TargetIdentifier();
    targetIdentifier.setE164Number(e164number);

    final var targetIdentifiers = new ListOfTargetIdentifiers();
    targetIdentifiers.getTargetIdentifier().add(targetIdentifier);

    final var dids = new ListOfDids();
    dids.getDId().add(destinationId);

    final var taskDetails = new TaskDetails();
    taskDetails.setXId(xId);
    taskDetails.setTargetIdentifiers(targetIdentifiers);

    taskDetails.setDeliveryType(DeliveryType.X_2_AND_X_3);
    taskDetails.setListOfDIDs(dids);
    return taskDetails;
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
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/updateTask")
  public ModifyTaskResponse updateTask(
    @RequestParam final String e164number,
    @RequestParam final String destinationId,
    @RequestParam final String xId
  ) throws X1ClientException, InterruptedException {
    final var taskDetails = makeTaskDetails(e164number, destinationId, xId);
    final var req = x1RequestFactory.create(ModifyTaskRequest.class);
    req.setTaskDetails(taskDetails);

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
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/deleteTask")
  public DeactivateTaskResponse deactivateTask(@RequestParam final String xId)
    throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.create(DeactivateTaskRequest.class);
    req.setXId(xId);

    return x1Client.request(req, DeactivateTaskResponse.class);
  }
}
