package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.X2X3Server;
import com.sipgate.li.simulator.config.SimulatorConfig;
import jakarta.annotation.PreDestroy;
import java.net.InetSocketAddress;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class X2X3ServerStarter {

  private final X2X3Server x2X3Server;
  private final SimulatorConfig simulatorConfig;

  public X2X3ServerStarter(final SimulatorConfig simulatorConfig, final X2X3Server x2X3Server) {
    this.simulatorConfig = simulatorConfig;
    this.x2X3Server = x2X3Server;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void startX2X3Server() throws InterruptedException {
    x2X3Server.start(new InetSocketAddress("0.0.0.0", simulatorConfig.getX2X3ServerConfig().port()));
  }

  @PreDestroy
  public void stopX2X3Server() {
    x2X3Server.stop();
  }
}
