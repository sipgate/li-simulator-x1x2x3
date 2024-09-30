package com.sipgate.li.simulator.controller;

import static com.sipgate.li.lib.x2x3.PduObject.MANDATORY_HEADER_LENGTH;

import com.sipgate.li.lib.x2x3.PayloadDirection;
import com.sipgate.li.lib.x2x3.PayloadFormat;
import com.sipgate.li.lib.x2x3.PduObject;
import com.sipgate.li.lib.x2x3.PduType;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/x2")
public class X2Controller {

  @GetMapping("/last")
  public ResponseEntity<PduObject> getLast() {
    final var result = new PduObject(
      (short) 0,
      (short) 5,
      PduType.X2_PDU,
      MANDATORY_HEADER_LENGTH,
      0,
      PayloadFormat.SIP,
      PayloadDirection.SENT_FROM_TARGET,
      UUID.randomUUID(),
      new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 },
      new byte[] {},
      new byte[] {}
    );
    return ResponseEntity.ok(result);
  }
}
