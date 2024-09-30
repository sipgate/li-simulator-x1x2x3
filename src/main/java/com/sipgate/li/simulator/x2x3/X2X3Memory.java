package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.PduObject;
import com.sipgate.li.simulator.event.X2X3ReceivedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class X2X3Memory {

  private PduObject lastMessage = null;

  public PduObject getLast() {
    return lastMessage;
  }

  @EventListener(X2X3ReceivedEvent.class)
  public void onX2X3Received(final X2X3ReceivedEvent event) {
    lastMessage = event.pduObject();
  }
}
