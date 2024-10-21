package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.client.X1ClientException;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class X1ExceptionAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(X1ExceptionAdvice.class);

  @ExceptionHandler(X1ClientException.class)
  public ResponseEntity<ErrorResponse> handleX1ClientException(final X1ClientException e) {
    LOGGER.error("X1ClientException", e);
    if (e.getTopLevelErrorResponse() == null) {
      return ResponseEntity.status(502).build();
    }

    return ResponseEntity.status(502).body(new ErrorResponse(e.getTopLevelErrorResponse()));
  }
}
