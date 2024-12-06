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
import java.util.List;
import org.etsi.uri._03221.x1._2017._10.ErrorResponse;
import org.etsi.uri._03221.x1._2017._10.ListAllDetailsRequest;
import org.etsi.uri._03221.x1._2017._10.ListAllDetailsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

  private final X1RequestFactory x1RequestFactory;
  private final X1Client x1Client;

  public IndexController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
    this.x1RequestFactory = x1RequestFactory;
    this.x1Client = x1Client;
  }

  public record IndexResponse(List<String> tasks, List<String> destinations) {}

  @Operation(
    summary = "ListAllDetailsRequest",
    description = "Used by the ADMF to retrieve the list of all XIDs and DIDs (i.e. a list of identifiers) but no details."
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "List of active tasks and destinations."),
      @ApiResponse(
        responseCode = "502",
        description = "The list was not returned properly.",
        content = @Content(schema = @Schema(implementation = SimulatorErrorResponse.class))
      ),
      @ApiResponse(
        responseCode = "400",
        description = "ErrorResponse was returned by the X1 server",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @GetMapping("/index")
  public IndexResponse index() throws X1ClientException, InterruptedException {
    final var req = x1RequestFactory.builder(ListAllDetailsRequest.builder()).build();
    final var resp = x1Client.request(req, ListAllDetailsResponse.class);
    return new IndexResponse(resp.getListOfXIDs().getXId(), resp.getListOfDIDs().getDId());
  }
}
