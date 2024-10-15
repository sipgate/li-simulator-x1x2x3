package com.sipgate.li.simulator.x2x3;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sipgate.li.lib.x2x3.PduValidationException;
import com.sipgate.li.lib.x2x3.X2X3Decoder;
import io.netty.channel.ChannelHandlerContext;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class X2X3NettyDecoderTest {

  @Mock
  X2X3Decoder x2X3Decoder;

  @InjectMocks
  X2X3NettyDecoder underTest;

  @Spy
  private ChannelHandlerContext channelHandlerContext;

  static Stream<Arguments> decoderExceptions() {
    return Stream.of(
      Arguments.arguments(new PduValidationException("something's broken", "it's your fault")),
      Arguments.arguments(new IllegalArgumentException("Header too large"))
    );
  }

  @ParameterizedTest
  @MethodSource("decoderExceptions")
  void it_closes_on_pdu_validation_exception(final Exception e) {
    // GIVEN
    doThrow(e).when(x2X3Decoder).decode(any());

    // WHEN
    underTest.decode(channelHandlerContext, null, null);

    // THEN
    verify(channelHandlerContext).close();
  }
}
