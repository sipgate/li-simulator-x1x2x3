package com.sipgate.li.simulator.e2e;

import static com.sipgate.li.lib.x2x3.PduObject.MANDATORY_HEADER_LENGTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sipgate.li.lib.x2x3.*;
import com.sipgate.li.simulator.x2x3.X2X3InboundHandlerAdapter;
import com.sipgate.li.simulator.x2x3.X2X3Server;
import com.sipgate.util.SSLContextBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.UUID;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

@Tag("E2E")
class ServerTlsTest {

  private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
  private final X2X3InboundHandlerAdapter x2x3InboundHandlerAdapter = new X2X3InboundHandlerAdapter(
    applicationEventPublisher
  );
  private final X2X3Decoder x2X3Decoder = new X2X3Decoder(2000, 2000);
  private final SSLContext serverSslContext = serverSslContext();
  private final SSLContext clientSslContext = clientSslContext();

  ServerTlsTest()
    throws NoSuchAlgorithmException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {}

  @Test
  void it_connects_to_x2x3server() throws Exception {
    X2X3Server server = new X2X3Server(x2x3InboundHandlerAdapter, x2X3Decoder, serverSslContext);

    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          server.run();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
    t.start();
    Thread.sleep(1000);

    SocketFactory clientSocketFactory = clientSslContext.getSocketFactory();
    final var client = new X2X3Client(clientSocketFactory, "localhost", 42069);
    PduObject pdu = new PduObject(
      (short) 0,
      (short) 5,
      PduType.X2_PDU,
      MANDATORY_HEADER_LENGTH,
      0,
      PayloadFormat.SIP,
      PayloadDirection.SENT_FROM_TARGET,
      UUID.randomUUID(),
      new byte[8],
      new byte[0],
      new byte[0]
    );
    client.send(pdu);
    t.join();

    System.err.println("Je voir here an tomasE");
  }

  SSLContext serverSslContext()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
    return SSLContextBuilder.newBuilder()
      .withKeyStore(Path.of("src/test/resources/server-keystore-test.p12"), "changeit")
      .withTrustStore(Path.of("src/test/resources/server-truststore-test.jks"), "changeit")
      .build();
  }

  SSLContext clientSslContext()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
    return SSLContextBuilder.newBuilder()
      .withKeyStore(Path.of("src/test/resources/client-keystore-test.p12"), "changeit")
      .withTrustStore(Path.of("src/test/resources/client-truststore-test.jks"), "changeit")
      .build();
  }
}
