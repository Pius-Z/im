package com.pius.im.codec;

import com.pius.im.codec.proto.Message;
import com.pius.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {

        ByteBuf content = msg.content();
        if (content.readableBytes() < 28) {
            return;
        }

        Message message = ByteBufToMessageUtils.transition(content);
        if (message == null) {
            return;
        }

        out.add(message);
    }
}
