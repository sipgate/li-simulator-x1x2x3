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
import org.etsi.uri._03221.x1._2017._10.ErrorResponse;
import org.etsi.uri._03221.x1._2017._10.KeepaliveRequest;
import org.etsi.uri._03221.x1._2017._10.KeepaliveResponse;
import org.etsi.uri._03221.x1._2017._10.PingRequest;
import org.etsi.uri._03221.x1._2017._10.PingResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionController {

  private final X1RequestFactory x1RequestFactory;
  private final X1Client x1Client;

  public ConnectionController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
    this.x1RequestFactory = x1RequestFactory;
    this.x1Client = x1Client;
  }

  @Operation(
    summary = "PingRequest",
    description = "Send a ping request to the NE. At any time from the ADMF or NE, to get a response over the X1 interface (does not test X2 or X3 or onward delivery, not a health check)."
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Ping to the NE was returned."),
      @ApiResponse(
        responseCode = "502",
        description = "The PingRequest was not returned properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/connection/ping")
  public PingResponse ping() throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.create(PingRequest.class);
    return x1Client.request(req, PingResponse.class);
  }

  @Operation(
    summary = "Keepalive",
    description = """
    The Keepalive functionality shall be supported by NE and ADMF. It is for prior agreement to determine whether
    Keepalives are enabled or disabled. By default, (with no prior agreement) they are enabled. It is intended as a means for
    the NE application to assert that the ADMF application is still operational, and, unless otherwise configured, remove all
    tasking information as a security measure if it is not.
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Keepalive to the NE was returned."),
      @ApiResponse(
        responseCode = "502",
        description = "The KeepaliveRequest was not returned properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/connection/keepalive")
  public KeepaliveResponse keepalive() throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.create(KeepaliveRequest.class);
    return x1Client.request(req, KeepaliveResponse.class);
  }
}
