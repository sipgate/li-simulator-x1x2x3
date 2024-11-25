package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.client.TopLevelErrorClientException;
import com.sipgate.li.lib.x1.client.X1Client;
import com.sipgate.li.lib.x1.client.X1ClientException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.etsi.uri._03221.x1._2017._10.ErrorResponse;
import org.etsi.uri._03221.x1._2017._10.TopLevelErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MalformedController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MalformedController.class);

  private final X1Client x1Client;

  public MalformedController(final X1Client x1Client) {
    this.x1Client = x1Client;
  }

  @Operation(
    summary = "MalformedX1Request",
    description = "Send a malformed X1 request in order to trigger a TopLevelErrorResponse"
  )
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "TopLevelErrorResponse received") })
  @PostMapping("/malformed/tler")
  public TopLevelErrorResponse topLevelErrorResponse() throws X1ClientException, InterruptedException {
    try {
      x1Client.request(null, ErrorResponse.class); // this should throw a TopLevelErrorClientException
      throw new IllegalStateException("Expected TopLevelErrorClientException was not thrown");
    } catch (final TopLevelErrorClientException e) {
      LOGGER.debug("The expected error occurred while sending X1 request", e);
      return e.getTopLevelErrorResponse();
    }
  }
}
