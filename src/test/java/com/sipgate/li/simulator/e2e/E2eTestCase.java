package com.sipgate.li.simulator.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Tag;

@Tag("E2E")
abstract class E2eTestCase {

  static final String DEFAULT_SERVICE_HOST = "localhost";
  static final String DEFAULT_SERVICE_PORT = "8080";
  final URI baseUri;
  final HttpClient client;
  final ObjectMapper objectMapper;

  E2eTestCase() {
    final String serviceHost = System.getProperty(
      "serviceHost",
      DEFAULT_SERVICE_HOST
    );
    final var servicePort = Integer.parseInt(
      System.getProperty("servicePort", DEFAULT_SERVICE_PORT)
    );

    baseUri = URI.create(
      String.format("http://%s:%d", serviceHost, servicePort)
    );
    client = HttpClient.newBuilder().build();

    objectMapper = new ObjectMapper();
  }

  HttpResponse<String> getUnauthenticated(final String path)
    throws IOException, InterruptedException {
    final var request = HttpRequest.newBuilder()
      .method("GET", HttpRequest.BodyPublishers.noBody())
      .uri(baseUri.resolve(path))
      .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  HttpResponse<String> postUnauthenticated(final String path)
    throws IOException, InterruptedException {
    return postUnauthenticated(path, Map.of());
  }

  HttpResponse<String> postUnauthenticated(
    final String path,
    final Map<String, String> formData
  ) throws IOException, InterruptedException {
    final var request = HttpRequest.newBuilder()
      .header("Content-Type", "application/x-www-form-urlencoded")
      .method(
        "POST",
        HttpRequest.BodyPublishers.ofString(mapFormDataToString(formData))
      )
      .uri(baseUri.resolve(path))
      .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  private static String mapFormDataToString(
    final Map<String, String> formData
  ) {
    final var formBodyBuilder = new StringBuilder();
    for (final var singleEntry : formData.entrySet()) {
      if (!formBodyBuilder.isEmpty()) {
        formBodyBuilder.append("&");
      }

      formBodyBuilder.append(
        URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8)
      );
      formBodyBuilder.append("=");
      formBodyBuilder.append(
        URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8)
      );
    }

    return formBodyBuilder.toString();
  }
}
