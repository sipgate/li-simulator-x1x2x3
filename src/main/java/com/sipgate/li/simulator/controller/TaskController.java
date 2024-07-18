package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import java.io.IOException;
import java.util.UUID;
import org.etsi.uri._03221.x1._2017._10.ActivateTaskRequest;
import org.etsi.uri._03221.x1._2017._10.ActivateTaskResponse;
import org.etsi.uri._03221.x1._2017._10.DeliveryType;
import org.etsi.uri._03221.x1._2017._10.ListOfDids;
import org.etsi.uri._03221.x1._2017._10.ListOfTargetIdentifiers;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.etsi.uri._03221.x1._2017._10.TargetIdentifier;
import org.etsi.uri._03221.x1._2017._10.TaskDetails;
import org.springframework.http.ResponseEntity;
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

  public record ActiveTaskResponse(OK ok, String xId) {}

  @PostMapping("/task")
  public ResponseEntity<ActiveTaskResponse> activateTask(
    @RequestParam final String e164number,
    @RequestParam final String destinationId
  ) throws IOException, InterruptedException {
    final var xId = UUID.randomUUID().toString();

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

    return ResponseEntity.ok(new ActiveTaskResponse(resp.getOK(), xId));
  }
}
