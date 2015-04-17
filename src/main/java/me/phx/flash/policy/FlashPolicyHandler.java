package me.phx.flash.policy;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.*;
import java.nio.channels.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author phoenix
 */
public class FlashPolicyHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log = Logger.getLogger(FlashPolicyHandler.class.getName());

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent state = (IdleStateEvent) evt;
            log.log(Level.INFO, "Closing timeout connection : " + ctx.channel().remoteAddress());
            if (state.state() == IdleState.READER_IDLE) {
                CleanUpUtil.closeOnFlush(ctx.channel());
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private boolean validate(String msg) {
        return msg.contains("<policy-file-request/>");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws IOException {
        if ("bye".equals(msg)) {
            ctx.writeAndFlush("Server shutdown \n").addListener(ChannelFutureListener.CLOSE);
            CleanUpUtil.closeOnFlush(ctx.channel().parent());
            return;
        }
        if (!validate(msg)) {
            ctx.writeAndFlush("Quest Error \0").addListener(ChannelFutureListener.CLOSE);
        }
        File policyFile = new File(FlashPolicyServer.POLICY_FILE);
        if (!policyFile.exists()) {
            log.log(Level.INFO, "Policy file not exist, create one");
            crete(policyFile);
        }
        try (FileInputStream fin = new FileInputStream(policyFile)) {
            FileRegion fileRegion = new DefaultFileRegion(fin.getChannel(), 0, policyFile.length());
            log.log(Level.INFO, "Sending policy file to : " + ctx.channel().remoteAddress());
            ctx.writeAndFlush(fileRegion).addListener(ChannelFutureListener.CLOSE);
        } catch (FileNotFoundException e) {
            CleanUpUtil.closeOnFlush(ctx.channel());
            CleanUpUtil.closeOnFlush(ctx.channel().parent());
        }
    }

    private void crete(File file) throws IOException {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream in = loader.getResourceAsStream("socket-policy.xml");
             ReadableByteChannel ch = Channels.newChannel(in)) {
            FileChannel outChannel = new FileOutputStream(file).getChannel();
            outChannel.transferFrom(ch, 0, in.available());
        }
    }
}
