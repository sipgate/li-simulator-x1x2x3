package com.sipgate.li.simulator.controller;

import static com.sipgate.li.lib.x2x3.PduObject.MANDATORY_HEADER_LENGTH;

import com.sipgate.li.lib.x2x3.PayloadDirection;
import com.sipgate.li.lib.x2x3.PayloadFormat;
import com.sipgate.li.lib.x2x3.PduObject;
import com.sipgate.li.lib.x2x3.PduType;
import com.sipgate.li.lib.x2x3.X2X3Client;
import com.sipgate.li.simulator.x2x3.X2X3Server;
import java.io.IOException;
import java.util.UUID;
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

  private final SSLContext x1x2x3SslContext;
  private final X2X3Server localX2X3Server;

  public SipController(final SSLContext x1x2x3SslContext, final X2X3Server localX2X3Server) {
    this.x1x2x3SslContext = x1x2x3SslContext;
    this.localX2X3Server = localX2X3Server;
  }

  @PostMapping("/sip")
  public ResponseEntity<String> interceptSip(@RequestBody final String sip) throws IOException {
    final var x2X3Client = makeX2X3Client();
    final byte[] bytes = sip.getBytes();
    final var request = new PduObject(
      (short) 0,
      (short) 5,
      PduType.X2_PDU,
      MANDATORY_HEADER_LENGTH,
      bytes.length,
      PayloadFormat.SIP,
      PayloadDirection.SENT_FROM_TARGET,
      UUID.randomUUID(),
      new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 },
      new byte[] {},
      bytes
    );
    x2X3Client.send(request);
    return ResponseEntity.ok("null");
  }

  private X2X3Client makeX2X3Client() throws IOException {
    LOGGER.info("Attempting to create local connection, port:{}", localX2X3Server.getPort());
    return new X2X3Client(x1x2x3SslContext.getSocketFactory(), "localhost", localX2X3Server.getPort());
  }
}
