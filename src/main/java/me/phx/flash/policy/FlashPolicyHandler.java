package me.phx.flash.policy;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by phoenix on 11/9/14.
 */
public class FlashPolicyHandler extends SimpleChannelInboundHandler<String> {
    private static String xml = "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">" +
            "<cross-domain-policy>"
            + "<allow-access-from domain=\"*\" to-ports=\"80,8080\"/>"
            + "</cross-domain-policy>\0";


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent state = (IdleStateEvent) evt;
            if (state.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    private boolean validate(String msg) {
        return msg.indexOf("<policy-file-request/>") >= 0;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (validate(msg)) {
            ctx.writeAndFlush(xml).addListener(ChannelFutureListener.CLOSE);
        } else {
            ctx.writeAndFlush("Quest Error \0").addListener(ChannelFutureListener.CLOSE);
        }
    }
}
