package com.sipgate.li.simulator.controller;

import com.sipgate.li.lib.x1.X1Client;
import com.sipgate.li.lib.x1.X1ClientException;
import com.sipgate.li.lib.x1.X1RequestFactory;
import com.sipgate.li.simulator.controller.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.UUID;
import org.etsi.uri._03221.x1._2017._10.*;
import org.etsi.uri._03280.common._2017._07.IPAddress;
import org.etsi.uri._03280.common._2017._07.IPAddressPort;
import org.etsi.uri._03280.common._2017._07.Port;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DestinationController {

  private final X1RequestFactory x1RequestFactory;
  private final X1Client x1Client;

  public DestinationController(final X1RequestFactory x1RequestFactory, final X1Client x1Client) {
    this.x1RequestFactory = x1RequestFactory;
    this.x1Client = x1Client;
  }

  @Operation(
    summary = "Create destination",
    description = """
      Used by the ADMF to add a new Destination to an NE
    """
  )
  @ApiResponses(
    value = {
      @ApiResponse(responseCode = "200", description = "Destination was created."),
      @ApiResponse(
        responseCode = "502",
        description = "The CreateDestination operation was not handled properly.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
    }
  )
  @PostMapping("/destination")
  public CreateDestinationResponse createDestination(
    @RequestParam final UUID dId,
    @RequestParam final String friendlyName,
    @RequestParam final DeliveryType deliveryType,
    @Parameter(
      description = "will be treated as ipv6 if there is a colon (:) in the ip address"
    ) @RequestParam final String ipAddress,
    @RequestParam final Integer tcpPort
  ) throws X1ClientException, InterruptedException {
    final DestinationDetails destinationDetails = makeDestinationDetails(
      dId,
      friendlyName,
      deliveryType,
      tcpPort,
      ipAddress
    );

    final var createDestinationRequest = x1RequestFactory.create(CreateDestinationRequest.class);
    createDestinationRequest.setDestinationDetails(destinationDetails);

    return x1Client.request(createDestinationRequest, CreateDestinationResponse.class);
  }

  private DestinationDetails makeDestinationDetails(
    final UUID dId,
    final String friendlyName,
    final DeliveryType deliveryType,
    final Integer tcpPort,
    final String ip
  ) {
    final var destinationDetails = new DestinationDetails();
    destinationDetails.setFriendlyName(friendlyName);
    destinationDetails.setDeliveryType(deliveryType);
    destinationDetails.setDId(dId.toString());

    final var port = new Port();
    port.setTCPPort(tcpPort);
    final IPAddress ipAddress = new IPAddress();

    if (ip.contains(":")) {
      ipAddress.setIPv6Address(ip);
    } else {
      ipAddress.setIPv4Address(ip);
    }

    final IPAddressPort ipAndPort = new IPAddressPort();
    ipAndPort.setPort(port);
    ipAndPort.setAddress(ipAddress);

    final DeliveryAddress address = new DeliveryAddress();
    address.setIpAddressAndPort(ipAndPort);
    destinationDetails.setDeliveryAddress(address);

    return destinationDetails;
  }
}
