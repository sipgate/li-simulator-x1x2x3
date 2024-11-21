package com.sipgate.li.simulator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sipgate.li.lib.x1.client.ErrorResponseClientException;
import com.sipgate.li.lib.x1.client.TopLevelErrorClientException;
import com.sipgate.li.lib.x1.client.X1ClientException;
import com.sipgate.li.simulator.controller.response.SimulatorErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class X1ExceptionAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(X1ExceptionAdvice.class);
  private final ObjectMapper objectMapper;

  public X1ExceptionAdvice(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @ExceptionHandler(X1ClientException.class)
  public void handleX1ClientException(final HttpServletResponse response, final X1ClientException exception)
    throws IOException {
    LOGGER.error("X1ClientException: {}", exception.getMessage());
    switch (exception) {
      case final TopLevelErrorClientException e -> {
        response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        try (final var outputStream = response.getOutputStream()) {
          objectMapper.writeValue(outputStream, new SimulatorErrorResponse(e.getTopLevelErrorResponse()));
        }
      }
      case final ErrorResponseClientException e -> {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (final var outputStream = response.getOutputStream()) {
          objectMapper.writeValue(outputStream, e.getErrorResponse());
        }
      }
      default -> {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try (final var printStream = new PrintStream(response.getOutputStream())) {
          exception.printStackTrace(printStream); // stack trace includes message
        }
      }
    }
  }
}
