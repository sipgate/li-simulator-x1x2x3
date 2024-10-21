package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.protocol.PduObject;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class X2X3Memory implements Consumer<PduObject> {

  private PduObject lastMessage = null;

  public PduObject getLast() {
    return lastMessage;
  }

  public void reset() {
    lastMessage = null;
  }

  @Override
  public void accept(final PduObject pduObject) {
    this.lastMessage = pduObject;
  }
}
