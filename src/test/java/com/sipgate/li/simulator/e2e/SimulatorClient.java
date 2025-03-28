/*
 * SPDX-License-Identifier: MIT
 */
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
    return exchange("GET", path, null, null, responseType, expectedStatusCode);
  }

  public <T> T post(final String path, final Class<T> responseType) throws IOException, InterruptedException {
    return post(path, Map.of(), responseType, 200);
  }

  public <T> T post(
    final String path,
    final Map<String, String> arguments,
    final Class<T> responseType,
    final int expectedStatusCode
  ) throws IOException, InterruptedException {
    return post(
      path,
      "application/x-www-form-urlencoded",
      mapFormDataToString(arguments),
      responseType,
      expectedStatusCode
    );
  }

  public <T> T post(final String path, final String body, final Class<T> responseType)
    throws IOException, InterruptedException {
    return post(path, "application/octet-stream", body, responseType, 200);
  }

  public <T> T post(
    final String path,
    final String contentType,
    final String content,
    final Class<T> responseType,
    final int expectedStatusCode
  ) throws IOException, InterruptedException {
    return exchange("POST", path, contentType, content, responseType, expectedStatusCode);
  }

  public <T> T delete(final String path, final Class<T> responseType) throws IOException, InterruptedException {
    return delete(path, responseType, 200);
  }

  public <T> T delete(final String path, final Class<T> responseType, final int expectedStatusCode)
    throws IOException, InterruptedException {
    return exchange("DELETE", path, null, null, responseType, expectedStatusCode);
  }

  private <T> T exchange(
    final String method,
    final String path,
    final String contentType,
    final String content,
    final Class<T> responseType,
    final int expectedStatusCode
  ) throws IOException, InterruptedException {
    final var requestBuilder = HttpRequest.newBuilder().uri(baseUri.resolve(path));
    if (content == null || content.isEmpty()) {
      requestBuilder.method(method, BodyPublishers.noBody());
    } else {
      requestBuilder.header("Content-Type", contentType);
      requestBuilder.method(method, BodyPublishers.ofString(content));
    }

    final var request = requestBuilder.build();
    final var response = httpClient.send(request, BodyHandlers.ofString());
    if (response.statusCode() != expectedStatusCode) {
      throw new IOException(
        "Unexpected response code: " + response.statusCode() + " (expected: " + expectedStatusCode + ")",
        new RemoteCause(response.body())
      );
    }

    final var responseBody = response.body();
    if (responseBody.isEmpty()) {
      return null;
    }

    if (responseType.equals(String.class)) {
      return responseType.cast(response.body());
    }

    try {
      return objectMapper.readValue(responseBody, responseType);
    } catch (final IOException | RuntimeException e) {
      throw new IOException(e.getMessage() + "\n\nResponse Body:\n" + responseBody, e);
    }
  }

  private static String mapFormDataToString(final Map<String, String> formData) {
    if (formData.isEmpty()) {
      return "";
    }
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

  public static class RemoteCause extends Throwable {

    public RemoteCause(final String message) {
      super(message, null, true, false);
    }
  }
}
