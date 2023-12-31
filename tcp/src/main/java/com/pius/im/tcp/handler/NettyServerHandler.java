package com.pius.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pius.im.codec.pack.LoginPack;
import com.pius.im.codec.pack.message.ChatMessageAck;
import com.pius.im.codec.proto.Message;
import com.pius.im.codec.proto.MessagePack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ImConnectStatusEnum;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.enums.command.SystemCommand;
import com.pius.im.common.model.UserClientDto;
import com.pius.im.common.model.UserSession;
import com.pius.im.common.model.message.CheckSendMessageReq;
import com.pius.im.tcp.feign.FeignMessageService;
import com.pius.im.tcp.publish.MessageProducer;
import com.pius.im.tcp.redis.RedisManager;
import com.pius.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private Integer brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(Integer brokerId, String logicUrl) {
        this.brokerId = brokerId;
        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .options(new Request.Options(1000, TimeUnit.MILLISECONDS, 3500, TimeUnit.MILLISECONDS, true))// 设置超时时间
                .target(FeignMessageService.class, logicUrl);
    }

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

            UserClientDto dto = new UserClientDto();
            dto.setAppId(message.getMessageHeader().getAppId());
            dto.setUserId(loginPack.getUserId());
            dto.setClientType(message.getMessageHeader().getClientType());
            dto.setImei(message.getMessageHeader().getImei());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSONObject.toJSONString(dto));
        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            // 登出command
            SessionSocketHolder.removeUserSession((NioSocketChannel) channelHandlerContext.channel());
        } else if (command == SystemCommand.PING.getCommand()) {
            channelHandlerContext.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            try {
                CheckSendMessageReq checkSendMessageReq = new CheckSendMessageReq();
                checkSendMessageReq.setAppId(message.getMessageHeader().getAppId());
                checkSendMessageReq.setCommand(message.getMessageHeader().getCommand());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(message.getMessagePack()));
                String fromId = jsonObject.getString("fromId");
                String toId;
                if (command == MessageCommand.MSG_P2P.getCommand()) {
                    toId = jsonObject.getString("toId");
                } else {
                    toId = jsonObject.getString("groupId");
                }
                checkSendMessageReq.setToId(toId);
                checkSendMessageReq.setFromId(fromId);

                ResponseVO responseVO = feignMessageService.checkSendMessage(checkSendMessageReq);
                if (responseVO.isOk()) {
                    MessageProducer.sendMessage(message, command);
                } else {
                    int ackCommand;
                    if (command == MessageCommand.MSG_P2P.getCommand()) {
                        ackCommand = MessageCommand.MSG_ACK.getCommand();
                    } else {
                        ackCommand = GroupEventCommand.GROUP_MSG_ACK.getCommand();
                    }

                    ChatMessageAck chatMessageAck = new ChatMessageAck(jsonObject.getString("messageId"));
                    ResponseVO<ChatMessageAck> ackResponseVO = new ResponseVO<>(responseVO.getCode(), responseVO.getMsg(), chatMessageAck);
                    MessagePack<ResponseVO<ChatMessageAck>> ack = new MessagePack<>();
                    ack.setData(ackResponseVO);
                    ack.setCommand(ackCommand);
                    channelHandlerContext.channel().writeAndFlush(ack);
                }
            } catch (Exception e) {
                log.error(Arrays.toString(e.getStackTrace()));
            }
        } else {
            MessageProducer.sendMessage(message, command);
        }

    }

    /**
     *  channel 处于不活动状态
     *  即用户离线，删除本地session并更新redis
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

}
