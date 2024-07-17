package com.sipgate.li.simulator.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Tag("E2E")
abstract class E2eTestCase {

    static final String DEFAULT_SERVICE_HOST = "localhost";
    static final String DEFAULT_SERVICE_PORT = "8080";
    final URI baseUri;
    final HttpClient client;
    final ObjectMapper objectMapper;

    E2eTestCase() {
        final String serviceHost = System.getProperty("serviceHost", DEFAULT_SERVICE_HOST);
        final var servicePort = Integer.parseInt(System.getProperty("servicePort", DEFAULT_SERVICE_PORT));

        baseUri = URI.create(String.format("http://%s:%d", serviceHost, servicePort));
        client = HttpClient.newBuilder().build();

        objectMapper = new ObjectMapper();
    }

    HttpResponse<String> sendUnauthenticated(final String path) throws IOException, InterruptedException {
        final var request =
                HttpRequest.newBuilder()
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .uri(baseUri.resolve(path))
                        .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
