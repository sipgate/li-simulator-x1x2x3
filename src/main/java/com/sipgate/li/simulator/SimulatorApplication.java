package com.sipgate.li.simulator;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
  info = @Info(
    title = "LI Simulator",
    version = "1.0",
    description = "A simulator for the ADMF part of X1/X2/X3 interfaces."
  )
)
public class SimulatorApplication {

  public static void main(final String[] args) {
    SpringApplication.run(SimulatorApplication.class, args);
  }
}
