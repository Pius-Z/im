package com.pius.im.codec;

import com.pius.im.codec.proto.Message;
import com.pius.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 请求头(指令 + 版本 + 客户端类型 + 消息解析类型 + appId + imeiLength + bodyLength)
        // imei
        // 请求体

        if (in.readableBytes() < 28) {
            return;
        }

        Message message = ByteBufToMessageUtils.transition(in);
        if (message == null) {
            return;
        }

        out.add(message);
    }
}

