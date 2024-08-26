package com.sipgate.li.simulator;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class SimulatorApplication {

  public static void main(String[] args) {
    Security.addProvider(new BouncyCastleProvider());

    SpringApplication.run(SimulatorApplication.class, args);
  }
}
