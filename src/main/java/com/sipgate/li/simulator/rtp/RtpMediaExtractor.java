/*
 * SPDX-License-Identifier: MIT
 */
package com.sipgate.li.simulator.rtp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RtpMediaExtractor {

  private static final int HEADER_SIZE = 12;
  private static final Map<Integer, String> PAYLOAD_TYPE_MAP = new HashMap<>();

  static {
    PAYLOAD_TYPE_MAP.put(0, "PCMU");
    PAYLOAD_TYPE_MAP.put(3, "GSM");
    PAYLOAD_TYPE_MAP.put(4, "G723");
    PAYLOAD_TYPE_MAP.put(5, "DVI4");
    PAYLOAD_TYPE_MAP.put(6, "DVI4");
    PAYLOAD_TYPE_MAP.put(7, "LPC");
    PAYLOAD_TYPE_MAP.put(8, "PCMA");
    PAYLOAD_TYPE_MAP.put(9, "G.722");
    PAYLOAD_TYPE_MAP.put(10, "L16");
    PAYLOAD_TYPE_MAP.put(11, "L16");
    PAYLOAD_TYPE_MAP.put(12, "QCELP");
    PAYLOAD_TYPE_MAP.put(13, "CN");
    PAYLOAD_TYPE_MAP.put(14, "MPA");
    PAYLOAD_TYPE_MAP.put(15, "G.728");
    PAYLOAD_TYPE_MAP.put(16, "DVI4");
    PAYLOAD_TYPE_MAP.put(17, "DVI4");
    PAYLOAD_TYPE_MAP.put(18, "G.729");
    PAYLOAD_TYPE_MAP.put(25, "CelB");
    PAYLOAD_TYPE_MAP.put(26, "JPEG");
    PAYLOAD_TYPE_MAP.put(28, "nv");
    PAYLOAD_TYPE_MAP.put(31, "H.261");
    PAYLOAD_TYPE_MAP.put(32, "MPV");
    PAYLOAD_TYPE_MAP.put(33, "MP2T");
    PAYLOAD_TYPE_MAP.put(34, "H.263");
  }

  private final Set<String> payloadTypeNames = new HashSet<>();

  public Set<String> getPayloadTypeNames() {
    return payloadTypeNames;
  }

  public void extractMediaFromRtp(final OutputStream output, final byte[] bytes) {
    try {
      final var payloadTypeCode = bytes[1] & 0x7F;
      payloadTypeNames.add(PAYLOAD_TYPE_MAP.get(payloadTypeCode));
      final var buf = new byte[bytes.length - HEADER_SIZE];
      System.arraycopy(bytes, HEADER_SIZE, buf, 0, bytes.length - HEADER_SIZE);
      output.write(buf);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
