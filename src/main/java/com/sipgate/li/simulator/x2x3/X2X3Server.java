package com.sipgate.li.simulator.x2x3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class X2X3Server {

  private static Logger LOGGER = LoggerFactory.getLogger(X2X3Server.class);

  final int port = 42069;

  private final X2X3InboundHandlerAdapter x2x3inboundHandlerAdapter;
  private final X2X3NettyDecoder x2x3nettyDecoder;

  public X2X3Server(
    final X2X3InboundHandlerAdapter x2x3InboundHandlerAdapter,
    final X2X3NettyDecoder x2X3NettyDecoder
  ) {
    this.x2x3inboundHandlerAdapter = x2x3InboundHandlerAdapter;
    this.x2x3nettyDecoder = x2X3NettyDecoder;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void run() throws Exception {
    final EventLoopGroup bossGroup = new NioEventLoopGroup();
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      final ServerBootstrap b = new ServerBootstrap();
      b
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(
          new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(final SocketChannel ch) throws Exception {
              ch.pipeline().addLast(x2x3nettyDecoder).addLast(x2x3inboundHandlerAdapter);
            }
          }
        )
        .option(ChannelOption.SO_BACKLOG, 128) // max pending connections
        .childOption(ChannelOption.SO_KEEPALIVE, true);

      // Bind and start to accept incoming connections.
      LOGGER.info("Listening socket on port {}", port);
      final ChannelFuture f = b.bind(port).sync();

      // Wait until the server socket is closed.
      // In this example, this does not happen, but you can do that to gracefully
      // shut down your server.
      f.channel().closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}
