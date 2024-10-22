package com.sipgate.li.simulator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sipgate.li.lib.x1.client.X1ClientException;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
  public void handleX1ClientException(final HttpServletResponse response, final X1ClientException e)
    throws IOException {
    LOGGER.error("X1ClientException: {}", e.getMessage());
    response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
    if (e.getTopLevelErrorResponse() == null) {
      try (final var printStream = new PrintStream(response.getOutputStream())) {
        e.printStackTrace(printStream); // stack trace includes message
      }

      return;
    }

    try (final var outputStream = response.getOutputStream()) {
      objectMapper.writeValue(outputStream, new ErrorResponse(e.getTopLevelErrorResponse()));
    }
  }
}
