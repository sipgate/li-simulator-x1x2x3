/*
 * SPDX-License-Identifier: MIT
 */
package com.sipgate.li.simulator.config;

import java.nio.file.Path;

public record SslStore(Path path, String password) {}
