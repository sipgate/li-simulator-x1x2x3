package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x2x3.protocol.PduObject;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import com.sipgate.li.simulator.x2x3.X2X3Memory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/x2")
public class X2Controller {

  private final X2X3Memory x2X3Memory;
  private Logger LOGGER = LoggerFactory.getLogger(X2Controller.class);

  public X2Controller(final X2X3Memory x2X3Memory) {
    this.x2X3Memory = x2X3Memory;
  }

  @Operation(summary = "Reset X2X3 Storage")
  @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "No Content") })
  @PostMapping("/reset")
  public ResponseEntity<Void> reset() throws IOException {
    x2X3Memory.reset();
    LOGGER.info("Reset completed");

    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get the last item from X2X3 Storage")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "Base64 encoded x2/x3 message as it was sent over the wire to the simulator"
      ),
      @ApiResponse(responseCode = "404", description = "The storage is empty"),
    }
  )
  @GetMapping("/last")
  public ResponseEntity<String> getLast() throws IOException {
    if (x2X3Memory.getLast() == null) {
      return ResponseEntity.notFound().build();
    }

    final var pduBytes = new ByteArrayOutputStream();
    final var pduStream = new DataOutputStream(pduBytes);
    x2X3Memory.getLast().writeTo(pduStream);

    return ResponseEntity.ok(Base64.getEncoder().encodeToString(pduBytes.toByteArray()));
  }

  @Operation(summary = "Get the full X2X3 Storage")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "List of Base64 encoded x2/x3 messages as they were sent over the wire to the simulator. Can be empty ([]) if storage is empty."
      ),
    }
  )
  @GetMapping("/all")
  public ResponseEntity<List<String>> getAll() throws IOException {
    final var respList = x2X3Memory
      .getStorage()
      .stream()
      .map(pdu -> {
        final var pduBytes = new ByteArrayOutputStream();
        final var pduStream = new DataOutputStream(pduBytes);

        try {
          pdu.writeTo(pduStream);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(pduBytes.toByteArray());
      })
      .toList();

    return ResponseEntity.ok(respList);
  }
}
