package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.X2X3Decoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X2X3NettyDecoder extends ByteToMessageDecoder {

  private static final Logger LOGGER = LoggerFactory.getLogger(X2X3NettyDecoder.class);

  private final X2X3Decoder x2x3Decoder;

  public X2X3NettyDecoder(final X2X3Decoder x2x3decoder) {
    this.x2x3Decoder = x2x3decoder;
  }

  @Override
  protected void decode(
    final ChannelHandlerContext channelHandlerContext,
    final ByteBuf byteBuf,
    final List<Object> out
  ) throws Exception {
    LOGGER.debug("Child handler decoding message: {}", byteBuf);

    try {
      x2x3Decoder.decode(byteBuf, out);
    } catch (final IllegalArgumentException e) {
      LOGGER.error(e.getMessage(), e);
      channelHandlerContext.close();
    }
  }
}
