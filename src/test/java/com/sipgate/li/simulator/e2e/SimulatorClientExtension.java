package com.sipgate.li.simulator.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorClientExtension extends TypeBasedParameterResolver<SimulatorClient> {

  private SimulatorClient simulatorClient;

  @Override
  public SimulatorClient resolveParameter(
    final ParameterContext parameterContext,
    final ExtensionContext extensionContext
  ) {
    if (simulatorClient != null) {
      return simulatorClient;
    }

    final var serviceHost = System.getProperty("serviceHost", "localhost");
    final var servicePort = Integer.parseInt(System.getProperty("servicePort", "8080"));

    final var baseUri = URI.create(String.format("http://%s:%d", serviceHost, servicePort));

    return (simulatorClient = new SimulatorClient(HttpClient.newHttpClient(), baseUri, new ObjectMapper()));
  }
}
