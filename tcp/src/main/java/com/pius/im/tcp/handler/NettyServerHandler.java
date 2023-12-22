package com.pius.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pius.im.codec.pack.LoginPack;
import com.pius.im.codec.proto.Message;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ImConnectStatusEnum;
import com.pius.im.common.enums.command.SystemCommand;
import com.pius.im.common.model.UserSession;
import com.pius.im.tcp.redis.RedisManager;
import com.pius.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.util.Arrays;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Slf4j
@AllArgsConstructor
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) {

        Integer command = message.getMessageHeader().getCommand();

        if (command == SystemCommand.LOGIN.getCommand()) {
            // 登录command

            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType());
            // 设置channel属性
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(loginPack.getUserId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(message.getMessageHeader().getAppId());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ClientType)).set(message.getMessageHeader().getClientType());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.Imei)).set(message.getMessageHeader().getImei());
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());

            // 创建session
            UserSession userSession = new UserSession();
            userSession.setAppId(message.getMessageHeader().getAppId());
            userSession.setClientType(message.getMessageHeader().getClientType());
            userSession.setUserId(loginPack.getUserId());
            userSession.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            userSession.setImei(message.getMessageHeader().getImei());
            userSession.setBrokerId(brokerId);
            try {
                userSession.setBrokerHost(InetAddress.getLocalHost().getHostAddress());
            } catch (Exception e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }

            // 将session存入缓存
            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<String, String> map = redissonClient.getMap(message.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + loginPack.getUserId());
            map.put(message.getMessageHeader().getClientType() + ":" + message.getMessageHeader().getImei(), JSONObject.toJSONString(userSession));

            // 保存channel
            SessionSocketHolder.put(message.getMessageHeader().getAppId(), loginPack.getUserId(), message.getMessageHeader().getClientType(),
                    message.getMessageHeader().getImei(), (NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            // 登出command
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        }

    }
}
