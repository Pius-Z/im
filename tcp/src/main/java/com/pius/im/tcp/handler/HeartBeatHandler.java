package com.pius.im.tcp.handler;

import com.pius.im.common.constant.Constants;
import com.pius.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Pius
 * @Date: 2023/12/21
 */
@Slf4j
@AllArgsConstructor
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Long heartBeatTime;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent event) {
            // 强制类型转换
            if (event.state() == IdleState.READER_IDLE) {
                log.info("读空闲");
            } else if (event.state() == IdleState.WRITER_IDLE) {
                log.info("写空闲");
            } else if (event.state() == IdleState.ALL_IDLE) {

                Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();
                long now = System.currentTimeMillis();

                if (lastReadTime != null && now - lastReadTime > heartBeatTime) {
                    SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
                }

            }
        }
    }
}
