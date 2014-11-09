package me.phx.flash.policy;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * Created by phoenix on 11/9/14.
 */
public enum StringCodec {

    UTF8(new StrCodec(CharsetUtil.UTF_8));

    private final ChannelDuplexHandler c;

    StringCodec(ChannelDuplexHandler c) {
        this.c = c;
    }

    public ChannelDuplexHandler getCodec() {
        return c;
    }
    @ChannelHandler.Sharable
    private static class StrCodec extends CombinedChannelDuplexHandler<StringDecoder, StringEncoder> {
        private StrCodec(Charset charset) {
            super(new StringDecoder(charset), new StringEncoder(charset));
        }
    }
}
