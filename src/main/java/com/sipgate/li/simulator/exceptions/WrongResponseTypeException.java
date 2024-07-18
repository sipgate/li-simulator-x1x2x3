package com.sipgate.li.simulator.exceptions;

import org.etsi.uri._03221.x1._2017._10.X1RequestMessage;
import org.etsi.uri._03221.x1._2017._10.X1ResponseMessage;

public class WrongResponseTypeException extends RuntimeException {
    public
        WrongResponseTypeException(final X1RequestMessage request, final Class<? extends X1ResponseMessage> expected, final X1ResponseMessage actual) {
        super(String.format("%s did not respond with %s, received %s", request.getClass().getSimpleName(), expected.getSimpleName(), actual.getClass().getSimpleName()));
    }
}
