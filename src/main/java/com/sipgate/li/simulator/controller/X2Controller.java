package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x2x3.PduObject;
import com.sipgate.li.simulator.x2x3.X2X3Memory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/x2")
public class X2Controller {

  private final X2X3Memory x2X3Memory;

  public X2Controller(final X2X3Memory x2X3Memory) {
    this.x2X3Memory = x2X3Memory;
  }

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
}
