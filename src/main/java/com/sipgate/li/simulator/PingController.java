package com.sipgate.li.simulator;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.etsi.uri._03221.x1._2017._10.PingRequest;
import org.etsi.uri._03221.x1._2017._10.PingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@ResponseBody
public class PingController {

    private final X1RequestFactory x1RequestFactory;
    private final X1Client x1Client;

    public PingController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
        this.x1RequestFactory = x1RequestFactory;
        this.x1Client = x1Client;
    }

    @Operation(summary = "PingRequest", description = "Send a ping request to the NE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ping to the NE was returned.", content = @Content(examples = @ExampleObject("""
                    {"ok": "AcknowledgedAndCompleted"}
                    """))),
            @ApiResponse(responseCode = "500", description = "The PingRequest was not returned properly.", content = @Content(examples = @ExampleObject("""
                    {"error": "PingRequest did not respond with PingResponse, received ActivateTaskResponse"}
                    """)))
    })
    @GetMapping("/ping")
    public ResponseEntity<Response> ping() throws Exception {

        final var req = x1RequestFactory.create(PingRequest.class);
        final var resp = x1Client.request(req);

        if (resp instanceof PingResponse p) {
            return ResponseEntity.ok(Response.ok(p.getOK()));
        }

        return ResponseEntity.internalServerError().body(Response.error("PingRequest did not respond with PingResponse, received " + resp.getClass().getSimpleName()));
    }

    record Response(String ok, String error) {
        static Response ok(OK ok) {
            return new Response(ok.value(), null);
        }

        static Response error(String m) {
            return new Response(null, m);
        }
    }
}
