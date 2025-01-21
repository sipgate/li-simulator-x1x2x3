/*
 * SPDX-License-Identifier: MIT
 */
package com.sipgate.li.simulator.e2e;

import org.junit.jupiter.api.BeforeEach;

/**
 * We want to force the order of these methods, so we implemented our own interface.
 * We need them separately for tests that need to do their own cleanup before our init.
 */
public interface StatefulTest {
  void cleanup() throws Exception;
  void init() throws Exception;

  @BeforeEach
  default void startup() throws Exception {
    cleanup();
    init();
  }
}
