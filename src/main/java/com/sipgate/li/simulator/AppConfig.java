package com.sipgate.li.simulator;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import jakarta.xml.bind.JAXBException;
import java.net.URI;
import java.net.http.HttpClient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

  @Bean
  public X1RequestFactory x1RequestFactory(
    @Value("${sipgate.li.simulator.ne.uri}") final URI neUri,
    @Value("${sipgate.li.simulator.admf.id:admfId}") final String admfIdentifier
  ) throws DatatypeConfigurationException {
    return new X1RequestFactory(
      DatatypeFactory.newInstance(),
      neUri.getHost(),
      admfIdentifier
    );
  }

  @Bean
  public X1Client x1Client(
    @Value("${sipgate.li.simulator.ne.uri}") final URI neUri
  ) throws JAXBException {
    LOGGER.info("Attempting to create connections to {}.", neUri);
    return new X1Client(neUri, HttpClient.newHttpClient());
  }
}
