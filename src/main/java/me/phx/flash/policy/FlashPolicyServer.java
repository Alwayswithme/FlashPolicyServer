package me.phx.flash.policy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author phoenix
 */
public class FlashPolicyServer {
    private static final Logger log = Logger.getLogger(FlashPolicyServer.class.getName());

    static final int PORT = Integer.parseInt(System.getProperty("port", "1843"));
    static final String POLICY_FILE = "socket-policy.xml";

    public void init(int port) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap().group(boss, worker);
            server.channel(NioServerSocketChannel.class)
                  .handler(new LoggingHandler(LogLevel.DEBUG));
            server.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(
                            // divide ByteBuf by NULL char
                            new DelimiterBasedFrameDecoder(1 << 10, Delimiters.nulDelimiter()),
                            // decode ByteBuf to String and encode String to ByteBuf
                            StringCodec.UTF8.getCodec(),
                            // trigger read timeout event
                            new IdleStateHandler(3, 0, 0),
                            // response the request
                            new FlashPolicyHandler());
                }
            });
            server.bind(port).sync().channel().closeFuture().sync();
        } finally {
            log.log(Level.INFO, "Going to delete policy file");
            CleanUpUtil.deleteFile(POLICY_FILE);
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        log.log(Level.INFO, "Server set up on port: " + PORT);
        new FlashPolicyServer().init(PORT);
    }
}
