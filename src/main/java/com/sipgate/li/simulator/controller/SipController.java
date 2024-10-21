package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x2x3.client.X2X3Client;
import com.sipgate.li.lib.x2x3.protocol.PayloadDirection;
import com.sipgate.li.lib.x2x3.protocol.PduObjectBuilder;
import com.sipgate.li.simulator.config.SimulatorConfig;
import java.io.IOException;
import javax.net.ssl.SSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SipController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SipController.class);

  private final SSLContext sslContext;
  private final int x2x3Port;

  public SipController(final SSLContext networkElementSslContext, final SimulatorConfig simulatorConfig) {
    this.sslContext = networkElementSslContext;
    this.x2x3Port = simulatorConfig.getX2X3ServerConfig().port();
  }

  @PostMapping("/sip")
  public ResponseEntity<String> interceptSip(@RequestBody final String sip) throws Exception {
    LOGGER.debug("interceptSip: {}", sip);
    try (final var x2X3Client = makeX2X3Client()) {
      final var request = new PduObjectBuilder()
        .payloadDirection(PayloadDirection.SENT_FROM_TARGET)
        .payload(sip.getBytes())
        .correlationID(new byte[8])
        .sip()
        .build();
      x2X3Client.send(request);
      return ResponseEntity.noContent().build();
    }
  }

  private X2X3Client makeX2X3Client() throws IOException {
    LOGGER.info("Attempting to create local connection, port:{}", x2x3Port);
    return new X2X3Client(sslContext.getSocketFactory(), "localhost", x2x3Port);
  }
}
