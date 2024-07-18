package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.etsi.uri._03221.x1._2017._10.KeepaliveRequest;
import org.etsi.uri._03221.x1._2017._10.KeepaliveResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class KeepaliveController {

    private final X1RequestFactory x1RequestFactory;
    private final X1Client x1Client;

    public KeepaliveController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
        this.x1RequestFactory = x1RequestFactory;
        this.x1Client = x1Client;
    }

    @Operation(summary = "Keepalive", description = """
            The Keepalive functionality shall be supported by NE and ADMF. It is for prior agreement to determine whether
            Keepalives are enabled or disabled. By default, (with no prior agreement) they are enabled. It is intended as a means for
            the NE application to assert that the ADMF application is still operational, and, unless otherwise configured, remove all
            tasking information as a security measure if it is not.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Keepalive to the NE was returned.", content = @Content(schema = @Schema(implementation = Response.class), examples = @ExampleObject("""
                    {"ok": "AcknowledgedAndCompleted"}
                    """))),
            @ApiResponse(responseCode = "500", description = "The KeepaliveRequest was not returned properly.", content = @Content(schema = @Schema(implementation = Response.class), examples = @ExampleObject("""
                    {"error": "KeepaliveRequest did not respond with KeepaliveResponse, received ActivateTaskResponse"}
                    """)))})
    @GetMapping("/keepalive")
    public ResponseEntity<Response> keepalive() throws Exception {
        final var req = x1RequestFactory.create(KeepaliveRequest.class);
        final var resp = x1Client.request(req, KeepaliveResponse.class);
        return ResponseEntity.ok(Response.ok(resp.getOK()));
    }
}
