package com.sipgate.li.simulator.controller;

import org.etsi.uri._03221.x1._2017._10.OK;

record Response(OK ok, String error) {
    static Response ok(OK ok) {
        return new Response(ok, null);
    }

    static Response error(String m) {
        return new Response(null, m);
    }
}