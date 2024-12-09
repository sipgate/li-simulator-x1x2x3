package com.sipgate.li.simulator.config;

import com.sipgate.li.lib.x1.client.X1Client;
import com.sipgate.li.lib.x1.client.X1ClientBuilder;
import com.sipgate.li.lib.x1.client.X1RequestFactory;
import com.sipgate.li.lib.x2x3.server.X2X3Decoder;
import com.sipgate.li.lib.x2x3.server.X2X3Server;
import com.sipgate.li.simulator.x2x3.X2X3Memory;
import com.sipgate.util.SSLContextBuilder;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sipgate.li.simulator")
public class SimulatorConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimulatorConfig.class);

  private URI targetUri;
  private String admfIdentifier;

  private SslStore clientCertKeyStore;
  private SslStore serverCertTrustStore;
  private X2X3ServerConfig x2X3ServerConfig;

  public void setTargetUri(final URI targetUri) {
    this.targetUri = targetUri;
  }

  public void setAdmfIdentifier(final String admfIdentifier) {
    this.admfIdentifier = admfIdentifier;
  }

  public void setClientCertKeyStore(final SslStore clientCertKeyStore) {
    this.clientCertKeyStore = clientCertKeyStore;
  }

  public void setServerCertTrustStore(final SslStore serverCertTrustStore) {
    this.serverCertTrustStore = serverCertTrustStore;
  }

  public void setX2X3Server(final X2X3ServerConfig x2X3ServerConfig) {
    this.x2X3ServerConfig = x2X3ServerConfig;
  }

  public X2X3ServerConfig getX2X3ServerConfig() {
    return x2X3ServerConfig;
  }

  @Bean
  public X1RequestFactory x1RequestFactory() throws DatatypeConfigurationException {
    return new X1RequestFactory(DatatypeFactory.newInstance(), targetUri.getHost(), admfIdentifier);
  }

  @Bean
  public X1Client x1Client(final SSLContext simulatorSslContext) {
    LOGGER.info("Attempting to create connections to {}.", targetUri);
    return X1ClientBuilder.newBuilder().withTarget(targetUri).withContext(simulatorSslContext).build();
  }

  @Bean
  public X2X3Server x2X3Server(final SSLContext simulatorSslContext, final X2X3Memory x2X3Memory) {
    return new X2X3Server(
      x2X3ServerConfig.sslEnabled ? simulatorSslContext : null,
      new X2X3Decoder(x2X3ServerConfig.maxHeaderLength, x2X3ServerConfig.maxPayloadLength)
    ).addConsumer(x2X3Memory);
  }

  @Bean
  SSLContext simulatorSslContext()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
    return SSLContextBuilder.newBuilder()
      .withKeyStore(clientCertKeyStore.path(), clientCertKeyStore.password())
      .withTrustStore(serverCertTrustStore.path(), serverCertTrustStore.password())
      .build();
  }

  public record X2X3ServerConfig(int maxHeaderLength, int maxPayloadLength, int port, boolean sslEnabled) {}
}
