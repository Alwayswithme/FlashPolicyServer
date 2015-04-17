package me.phx.flash.policy;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * @author phoenix
 */
public class CleanUpUtil {
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void closeQuietly(Closeable c) {
        try {
            if (c != null)
                c.close();
        } catch (IOException ignored) {
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(FlashPolicyServer.POLICY_FILE);
        return file.exists() && file.delete();
    }
}
