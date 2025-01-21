/*
 * SPDX-License-Identifier: MIT
 */
package com.sipgate.li.simulator.config;

import com.sipgate.util.SSLContextBuilder;
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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sipgate.li.network-element")
public class NetworkElementConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetworkElementConfig.class);

  private SslStore clientCertKeyStore;
  private SslStore serverCertTrustStore;

  public SslStore getClientCertKeyStore() {
    return clientCertKeyStore;
  }

  public void setClientCertKeyStore(final SslStore clientCertKeyStore) {
    this.clientCertKeyStore = clientCertKeyStore;
  }

  public void setServerCertTrustStore(final SslStore serverCertTrustStore) {
    this.serverCertTrustStore = serverCertTrustStore;
  }

  @Bean
  SSLContext networkElementSslContext()
    throws UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, NoSuchProviderException, KeyManagementException {
    LOGGER.debug("using keystore: {}", clientCertKeyStore.path());
    LOGGER.debug("using trust store: {}", serverCertTrustStore.path());

    return SSLContextBuilder.newBuilder()
      .withKeyStore(clientCertKeyStore.path(), clientCertKeyStore.password())
      .withTrustStore(serverCertTrustStore.path(), serverCertTrustStore.password())
      .build();
  }
}
