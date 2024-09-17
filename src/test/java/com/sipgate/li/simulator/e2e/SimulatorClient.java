package com.sipgate.li.simulator.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimulatorClient {

  private final HttpClient httpClient;
  private final URI baseUri;
  private final ObjectMapper objectMapper;

  SimulatorClient(final HttpClient httpClient, final URI baseUri, final ObjectMapper objectMapper) {
    this.httpClient = httpClient;
    this.baseUri = baseUri;
    this.objectMapper = objectMapper;
  }

  public <T> T get(final String path, final Class<T> responseType) throws IOException, InterruptedException {
    return get(path, responseType, 200);
  }

  public <T> T get(final String path, final Class<T> responseType, final int expectedStatusCode)
    throws IOException, InterruptedException {
    final var request = HttpRequest.newBuilder().uri(baseUri.resolve(path)).GET().build();

    final var response = httpClient.send(request, BodyHandlers.ofString());
    if (response.statusCode() != expectedStatusCode) {
      throw new IOException("Unexpected response code: " + response.statusCode());
    }

    final var responseBody = response.body();

    return objectMapper.readValue(responseBody, responseType);
  }

  <T> T post(final String path, final Class<T> responseType) throws IOException, InterruptedException {
    return post(path, Map.of(), responseType, 200);
  }

  <T> T post(final String path, final Map<String, String> arguments, final Class<T> responseType)
    throws IOException, InterruptedException {
    return post(path, arguments, responseType, 200);
  }

  <T> T post(
    final String path,
    final Map<String, String> arguments,
    final Class<T> responseType,
    final int expectedStatusCode
  ) throws IOException, InterruptedException {
    final var requestBuilder = HttpRequest.newBuilder().uri(baseUri.resolve(path)).POST(BodyPublishers.noBody());

    if (!arguments.isEmpty()) {
      requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
      requestBuilder.POST(BodyPublishers.ofString(mapFormDataToString(arguments)));
    }

    final var request = requestBuilder.build();

    final var response = httpClient.send(request, BodyHandlers.ofString());
    if (response.statusCode() != expectedStatusCode) {
      throw new IOException("Unexpected response code: " + response.statusCode());
    }

    final var responseBody = response.body();

    return objectMapper.readValue(responseBody, responseType);
  }

  private static String mapFormDataToString(final Map<String, String> formData) {
    final var formBodyBuilder = new StringBuilder();
    for (final var singleEntry : formData.entrySet()) {
      if (!formBodyBuilder.isEmpty()) {
        formBodyBuilder.append("&");
      }

      formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
      formBodyBuilder.append("=");
      formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
    }

    return formBodyBuilder.toString();
  }
}
