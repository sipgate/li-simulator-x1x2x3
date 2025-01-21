/*
 * SPDX-License-Identifier: MIT
 */
package com.sipgate.li.simulator.controller.response;

import org.etsi.uri._03221.x1._2017._10.TopLevelErrorResponse;

public record SimulatorErrorResponse(TopLevelErrorResponse error) {}
