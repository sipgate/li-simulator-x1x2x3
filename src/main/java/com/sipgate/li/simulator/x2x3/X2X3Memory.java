package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.protocol.PduObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class X2X3Memory implements Consumer<PduObject> {

  private final List<PduObject> storage = new ArrayList<>();

  public List<PduObject> getStorage() {
    return Collections.unmodifiableList(storage);
  }

  public PduObject getLast() {
    return storage.isEmpty() ? null : storage.getLast();
  }

  public void reset() {
    storage.clear();
  }

  @Override
  public void accept(final PduObject pduObject) {
    storage.add(pduObject);
  }
}
