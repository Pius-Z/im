package com.pius.im.tcp.handler;

import com.pius.im.codec.proto.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {
        System.out.println(message);
    }
}
