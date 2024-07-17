package com.sipgate.li.simulator.exceptions;

import org.etsi.uri._03221.x1._2017._10.X1RequestMessage;
import org.etsi.uri._03221.x1._2017._10.X1ResponseMessage;

public class WrongResponseTypeException extends RuntimeException {
    public <R extends X1RequestMessage, E extends X1ResponseMessage, A extends X1ResponseMessage>
        WrongResponseTypeException(Class<R> request, Class<E> expected, A actual) {
        super(String.format("{} did not respond with {}, received {}", request.getSimpleName(), expected.getSimpleName(), actual.getClass().getSimpleName()));
    }
}
