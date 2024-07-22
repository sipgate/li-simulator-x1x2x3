package com.sipgate.li.simulator.controller.response;

import org.etsi.uri._03221.x1._2017._10.ActivateTaskResponse;

public record TaskActivatedResponse(
  ActivateTaskResponse activateTaskResponse, String xId) {
}
