package com.sipgate.li.simulator.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SipController {

  @PostMapping("/sip")
  public ResponseEntity<String> interceptSip(@RequestBody final String sip) {
    return ResponseEntity.ok("{}");
  }
}
