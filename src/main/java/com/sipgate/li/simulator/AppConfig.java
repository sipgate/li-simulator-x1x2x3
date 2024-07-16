package com.sipgate.li.simulator;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1RequestFactory;
import jakarta.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.net.URI;
import java.net.http.HttpClient;

@Configuration
public class AppConfig {

    @Bean
    public X1RequestFactory x1RequestFactory(
            @Value("${sipgate.li.simulator.neUri}") final URI neUri,
            @Value("${sipgate.li.simulator.admfIdentifier:admfId}") final String admfIdentifier) throws DatatypeConfigurationException {

        return new X1RequestFactory(DatatypeFactory.newInstance(), neUri.getHost(), admfIdentifier);
    }

    @Bean
    public X1Client x1Client(@Value("${sipgate.li.simulator.neUri}") final URI neUri) throws JAXBException {
        return new X1Client(neUri, HttpClient.newHttpClient());
    }
}
