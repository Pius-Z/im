package com.pius.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.proto.MessagePack;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.command.Command;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Author: Pius
 * @Date: 2023/12/25
 */
@Slf4j
@Service
public class MessageProducer {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitMQConstants.MessageService2Im;

    public boolean sendMessage(UserSession session, Object msg) {
        try {
            log.info("send message == " + msg);
            rabbitTemplate.convertAndSend(queueName, session.getBrokerId() + "", msg);
            return true;
        } catch (Exception e) {
            log.error("send error :" + e.getMessage());
            return false;
        }
    }

    public boolean sendPack(String toId, Command command, Object msg, UserSession session) {
        MessagePack<JSONObject> messagePack = new MessagePack<>();
        messagePack.setCommand(command.getCommand());
        messagePack.setToId(toId);
        messagePack.setClientType(session.getClientType());
        messagePack.setAppId(session.getAppId());
        messagePack.setImei(session.getImei());
        messagePack.setData(JSONObject.parseObject(JSONObject.toJSONString(msg)));

        return sendMessage(session, JSONObject.toJSONString(messagePack));
    }

    /**
     * 根据指定参数是否为空判断是app调用还是后台管理员调用
     * app：发送给除当前端的所有端
     * 后台管理员：发送给所有端
     */
    public void sendToUser(String toId, Integer clientType, String imei, Command command, Object data, Integer appId) {
        if (clientType != null && StringUtils.isNotBlank(imei)) {
            ClientInfo clientInfo = new ClientInfo(appId, clientType, imei);
            sendToUserExceptClient(toId, command, data, clientInfo);
        } else {
            sendToUser(toId, command, data, appId);
        }
    }

    /**
     * 发送给所有端
     */
    public List<ClientInfo> sendToUser(String toId, Command command, Object data, Integer appId) {
        List<ClientInfo> list = new ArrayList<>();

        for (UserSession userSession : userSessionUtils.getUserSession(appId, toId)) {
            boolean b = sendPack(toId, command, data, userSession);
            if (b) {
                list.add(new ClientInfo(userSession.getAppId(), userSession.getClientType(), userSession.getImei()));
            }
        }

        return list;
    }

    /**
     * 发送给指定端
     */
    public void sendToUser(String toId, Command command, Object data, ClientInfo clientInfo) {
        UserSession userSession = userSessionUtils.getUserSession(clientInfo.getAppId(), toId, clientInfo.getClientType(),
                clientInfo.getImei());
        sendPack(toId, command, data, userSession);
    }

    /**
     * 发送给除了指定端的其他端
     */
    public void sendToUserExceptClient(String toId, Command command, Object data, ClientInfo clientInfo) {
        for (UserSession userSession : userSessionUtils.getUserSession(clientInfo.getAppId(), toId)) {
            if (!isMatch(userSession, clientInfo)) {
                sendPack(toId, command, data, userSession);
            }
        }
    }

    private boolean isMatch(UserSession userSession, ClientInfo clientInfo) {
        return Objects.equals(userSession.getAppId(), clientInfo.getAppId())
                && Objects.equals(userSession.getImei(), clientInfo.getImei())
                && Objects.equals(userSession.getClientType(), clientInfo.getClientType());
    }

}
