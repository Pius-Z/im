package com.pius.im.tcp.receiver.process;

import com.pius.im.codec.proto.MessagePack;
import com.pius.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
public abstract class BaseProcess {

    public abstract void processBefore();

    public void process(MessagePack messagePack) {
        processBefore();
        NioSocketChannel channel = SessionSocketHolder.get(messagePack.getAppId(),
                messagePack.getToId(), messagePack.getClientType(), messagePack.getImei());
        if (channel != null) {
            channel.writeAndFlush(messagePack);
        }
        processAfter();
    }

    public abstract void processAfter();

}
