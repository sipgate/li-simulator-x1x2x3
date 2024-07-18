package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.simulator.exceptions.WrongResponseTypeException;
import jakarta.xml.bind.JAXBException;
import org.etsi.uri._03221.x1._2017._10.PingResponse;
import org.etsi.uri._03221.x1._2017._10.X1RequestMessage;
import org.etsi.uri._03221.x1._2017._10.X1ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Function;

@Service
public class RequestWrapper {

    private X1Client x1Client;

    public RequestWrapper(final X1Client x1Client) {
        this.x1Client = x1Client;
    }

    @SuppressWarnings("unchecked")
    public <T, R extends X1ResponseMessage> ResponseEntity<T> request(
            X1RequestMessage req,
            Function<R, ResponseEntity<T>> responseFunction)
            throws JAXBException, IOException, InterruptedException {

        final var resp = x1Client.request(req);

        Class<? extends X1ResponseMessage> type = ((R) new Object()).getClass();
        if (type.isInstance(resp)) {
            throw new WrongResponseTypeException(req, PingResponse.class, resp);
        }

        return responseFunction.apply((R) resp);
    }
}
