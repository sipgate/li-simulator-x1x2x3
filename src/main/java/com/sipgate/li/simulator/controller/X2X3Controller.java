package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x2x3.protocol.PayloadDirection;
import com.sipgate.li.lib.x2x3.protocol.PayloadFormat;
import com.sipgate.li.lib.x2x3.protocol.PduObject;
import com.sipgate.li.lib.x2x3.protocol.PduType;
import com.sipgate.li.simulator.rtp.RtpMediaExtractor;
import com.sipgate.li.simulator.x2x3.X2X3Memory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/x2x3")
public class X2X3Controller {

  private final X2X3Memory x2X3Memory;
  private final Logger LOGGER = LoggerFactory.getLogger(X2X3Controller.class);

  public X2X3Controller(final X2X3Memory x2X3Memory) {
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

  @Operation(summary = "Get all items from the X2X3 Storage")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "List of Base64 encoded x2/x3 messages as they were sent over the wire to the simulator. Can be empty ([]) if there are none."
      ),
    }
  )
  @GetMapping("/all")
  public ResponseEntity<List<String>> getAll() throws IOException {
    final var respList = getStorageAsList(pdu -> true);
    return ResponseEntity.ok(respList);
  }

  @Operation(summary = "Get all X3 items from the X2X3 Storage")
  @ApiResponses(
    value = {
      @ApiResponse(
        responseCode = "200",
        description = "List of Base64 encoded x3 messages as they were sent over the wire to the simulator. Can be empty ([]) if there are none."
      ),
    }
  )
  @GetMapping("/all/x3")
  public ResponseEntity<List<String>> getAllX3() throws IOException {
    final var respList = getStorageAsList(pdu -> PduType.X3_PDU.equals(pdu.pduType()));
    return ResponseEntity.ok(respList);
  }

  // ================================

  @GetMapping(value = "/all/rtp/{xid}/{direction}")
  public ResponseEntity<byte[]> getAllRtp(@PathVariable final UUID xid, @PathVariable final String direction)
    throws IOException {
    final var liMediaExtractor = new RtpMediaExtractor();
    final var payloadDirection = "in".equals(direction)
      ? PayloadDirection.SENT_TO_TARGET
      : PayloadDirection.SENT_FROM_TARGET;
    try (final var buf = new ByteArrayOutputStream()) {
      x2X3Memory
        .getStorage()
        .stream()
        .filter(pdu -> PduType.X3_PDU.equals(pdu.pduType()))
        .filter(pdu -> PayloadFormat.RTP.equals(pdu.payloadFormat()))
        .filter(pdu -> xid.equals(pdu.xid()))
        .filter(pdu -> payloadDirection.equals(pdu.payloadDirection()))
        .sorted(Comparator.comparingInt(pdu -> findSequenceNumber(pdu).orElse(-1)))
        .map(PduObject::payload)
        .forEach(payload -> liMediaExtractor.extractMediaFromRtp(buf, payload));
      final var payloadTypeNames = String.join(",", liMediaExtractor.getPayloadTypeNames());
      LOGGER.info("Extracted types: {}", payloadTypeNames);
      return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM) // TODO: set the content type from getPayloadTypeNames()?
        .header("X-Payload-Types", payloadTypeNames)
        .body(buf.toByteArray());
    }
  }

  // ================================

  public Optional<Integer> findSequenceNumber(final PduObject pdu) {
    for (final var tlv : pdu.conditionalAttributeFields()) {
      if (tlv.getType() == 8) {
        final var baos = new ByteArrayOutputStream();
        final var outputStream = new DataOutputStream(baos);
        try {
          tlv.writeValueTo(outputStream);
          outputStream.flush();
          final var byteArray = baos.toByteArray();
          final int sequenceNumber = ByteBuffer.wrap(byteArray).getInt();
          return Optional.of(sequenceNumber);
        } catch (final Exception e) {
          LOGGER.trace("Error while extracting sequence number", e);
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  // ================================

  private List<String> getStorageAsList(final Predicate<PduObject> filter) {
    return x2X3Memory
      .getStorage()
      .stream()
      .filter(filter)
      .map(pdu -> {
        final var pduBytes = new ByteArrayOutputStream();
        final var pduStream = new DataOutputStream(pduBytes);

        try {
          pdu.writeTo(pduStream);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(pduBytes.toByteArray());
      })
      .toList();
  }
}
