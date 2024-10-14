package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x2x3.PayloadDirection;
import com.sipgate.li.lib.x2x3.PduObjectBuilder;
import com.sipgate.li.lib.x2x3.X2X3Client;
import com.sipgate.li.simulator.x2x3.X2X3Server;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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

  private final X2X3Server localX2X3Server;
  private final SSLContext sslContext;

  public SipController(final X2X3Server localX2X3Server, final SSLContext networkElementSslContext) {
    this.localX2X3Server = localX2X3Server;
    this.sslContext = networkElementSslContext;
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
    LOGGER.info("Attempting to create local connection, port:{}", localX2X3Server.getPort());
    return new X2X3Client(sslContext.getSocketFactory(), "localhost", localX2X3Server.getPort());
  }
}
