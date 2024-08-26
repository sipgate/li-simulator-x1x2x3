package com.sipgate.li.simulator;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1ClientBuilder;
import com.sipgate.li.lib.x1.X1RequestFactory;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import jakarta.xml.bind.JAXBException;
import java.net.URI;
import java.nio.file.Path;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sipgate.li.simulator")
@OpenAPIDefinition(
  info = @Info(
    title = "LI Simulator",
    version = "1.0",
    description = "A simulator for the ADMF part of X1/X2/X3 interfaces."
  )
)
public class AppConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

  private URI targetUri;
  private String admfIdentifier;

  private SslStore clientCertKeyStore;
  private SslStore serverCertTrustStore;

  public record SslStore(Path path, String password) {}

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

  @Bean
  public X1RequestFactory x1RequestFactory()
    throws DatatypeConfigurationException {
    return new X1RequestFactory(
      DatatypeFactory.newInstance(),
      targetUri.getHost(),
      admfIdentifier
    );
  }

  @Bean
  public X1Client x1Client() throws JAXBException {
    LOGGER.info("Attempting to create connections to {}.", targetUri);
    return X1ClientBuilder.newBuilder()
      .withTarget(targetUri)
      .withKeyStoreProvider("BC")
      .withKeyStore(clientCertKeyStore.path(), clientCertKeyStore.password())
      .withTrustStoreProvider("BC")
      .withTrustStore(
        serverCertTrustStore.path(),
        serverCertTrustStore.password()
      )
      .build();
  }
}
