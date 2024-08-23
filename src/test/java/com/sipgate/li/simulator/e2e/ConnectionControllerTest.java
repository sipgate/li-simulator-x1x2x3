package com.sipgate.li.simulator.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.stream.Stream;
import org.etsi.uri._03221.x1._2017._10.KeepaliveResponse;
import org.etsi.uri._03221.x1._2017._10.OK;
import org.etsi.uri._03221.x1._2017._10.PingResponse;
import org.etsi.uri._03221.x1._2017._10.X1ResponseMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(SimulatorClientExtension.class)
@Tag("E2E")
class ConnectionControllerTest {

  public static Stream<Arguments> providePathsAndResponseTypes() {
    return Stream.of(
      Arguments.of("connection/ping", PingResponse.class),
      Arguments.of("connection/keepalive", KeepaliveResponse.class)
    );
  }

  @ParameterizedTest
  @MethodSource("providePathsAndResponseTypes")
  void itReturns200ToRequests(
    final String path,
    final Class<X1ResponseMessage> responseType,
    final SimulatorClient client
  ) throws IOException, InterruptedException {
    // WHEN
    final var response = client.post(path, responseType);

    // THEN
    switch (response) {
      case final KeepaliveResponse keepaliveResponse:
        assertThat(keepaliveResponse.getOK()).isEqualTo(
          OK.ACKNOWLEDGED_AND_COMPLETED
        );
        break;
      case final PingResponse pingResponse:
        assertThat(pingResponse.getOK()).isEqualTo(
          OK.ACKNOWLEDGED_AND_COMPLETED
        );
        break;
      default:
        throw new RuntimeException(
          "Unexpected response type: " + response.getClass()
        );
    }
  }
}
