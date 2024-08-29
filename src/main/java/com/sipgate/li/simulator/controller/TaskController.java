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
import java.util.UUID;
import org.etsi.uri._03221.x1._2017._10.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

  private final X1RequestFactory x1RequestFactory;
  private final X1Client x1Client;

  public TaskController(
    final X1RequestFactory x1RequestFactory,
    final X1Client x1Client
  ) {
    this.x1RequestFactory = x1RequestFactory;
    this.x1Client = x1Client;
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
        responseCode = "500",
        description = "The KeepaliveRequest was not returned properly.",
        content = @Content(
          schema = @Schema(implementation = ErrorResponse.class)
        )
      ),
    }
  )
  @PostMapping("/task")
  public TaskActivatedResponse activateTask(
    @RequestParam final String e164number,
    @RequestParam final String destinationId,
    @RequestParam final String xId
  ) throws X1ClientException, InterruptedException {
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

    final var req = x1RequestFactory.create(ActivateTaskRequest.class);
    req.setTaskDetails(taskDetails);

    final var resp = x1Client.request(req, ActivateTaskResponse.class);
    return new TaskActivatedResponse(resp, xId);
  }
}
