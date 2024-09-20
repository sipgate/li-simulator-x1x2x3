package com.sipgate.li.simulator.x2x3;

import com.sipgate.li.lib.x2x3.PduObject;
import com.sipgate.li.simulator.event.X2X3ReceivedEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class X2X3InboundHandlerAdapter extends ChannelInboundHandlerAdapter {

  private static Logger LOGGER = LoggerFactory.getLogger(X2X3InboundHandlerAdapter.class);

  private final ApplicationEventPublisher applicationEventPublisher;

  public X2X3InboundHandlerAdapter(final ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
    try {
      LOGGER.debug("Received message: {}", msg);
      if (msg instanceof final PduObject pduObject) {
        applicationEventPublisher.publishEvent(new X2X3ReceivedEvent(pduObject));
      } else {
        throw new IllegalArgumentException("Received message is not an instance of X2X3PduObject");
      }
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }
}
