package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import com.sipgate.li.simulator.exceptions.WrongResponseTypeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.xml.bind.JAXBException;
import org.etsi.uri._03221.x1._2017._10.ListAllDetailsRequest;
import org.etsi.uri._03221.x1._2017._10.ListAllDetailsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Controller
@ResponseBody
public class ListAllDetailsController {

    private final X1RequestFactory x1RequestFactory;
    private final X1Client x1Client;

    public ListAllDetailsController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
        this.x1RequestFactory = x1RequestFactory;
        this.x1Client = x1Client;
    }

    record Response(List<String> tasks, List<String> destinations) {}


    @Operation(summary = "ListAllDetailsRequest", description = "Used by the ADMF to retrieve the list of all XIDs and DIDs (i.e. a list of identifiers) but no details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active tasks and destinations.", content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "500", description = "The list was not returned properly.", content = @Content(examples = @ExampleObject("""
                    {"error": "ListAllDetailsRequest did not respond with ListAllDetailsResponse, received something else."}
                    """)))
    })
    @GetMapping("/listAllDetails")
    public ResponseEntity<Response> allDetails() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, JAXBException, IOException, InterruptedException {
        final var req = x1RequestFactory.create(ListAllDetailsRequest.class);
        final var resp = x1Client.request(req);

        if (resp instanceof ListAllDetailsResponse ladr) {
            return ResponseEntity.ok(new Response(ladr.getListOfXIDs().getXId(), ladr.getListOfDIDs().getDId()));
        }

        throw new WrongResponseTypeException(req, ListAllDetailsResponse.class, resp);
    }
}
