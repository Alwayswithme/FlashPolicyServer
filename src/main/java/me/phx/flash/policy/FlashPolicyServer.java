package me.phx.flash.policy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by phoenix on 11/9/14.
 */
public class FlashPolicyServer {

    public void init(int port) throws InterruptedException {

        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap().group(boss, worker);
            server.channel(NioServerSocketChannel.class);
            server.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(
                            // 将ByteBuf按NULL字符分割
                            new DelimiterBasedFrameDecoder(1 << 10, Delimiters.nulDelimiter()),
                            // 读写内容转换为String
                            StringCodec.UTF8.getCodec(),
                            // 触发读超时事件
                            new IdleStateHandler(3, 0, 0),
                            // 返回策略文件内容
                            new FlashPolicyHandler());
                }
            });
            server.bind(port).sync().channel().closeFuture().sync();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new FlashPolicyServer().init(1843);
    }
}
