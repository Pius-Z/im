package com.pius.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.proto.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
public class WebSocketMessageEncoder extends MessageToMessageEncoder<MessagePack> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessagePack msg, List<Object> out) {
        String s = JSONObject.toJSONString(msg);
        ByteBuf byteBuf = Unpooled.directBuffer(8 + s.length());
        byte[] bytes = s.getBytes();
        byteBuf.writeInt(msg.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        out.add(new BinaryWebSocketFrame(byteBuf));
    }
}
