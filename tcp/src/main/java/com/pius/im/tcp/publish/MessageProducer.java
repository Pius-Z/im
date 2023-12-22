package com.pius.im.tcp.publish;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.proto.Message;
import com.pius.im.common.constant.Constants;
import com.pius.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Pius
 * @Date: 2023/12/21
 */
@Slf4j
public class MessageProducer {

    public static void sendMessage(Message message, Integer command) {
        Channel channel;
        String channelName = Constants.RabbitMQConstants.Im2MessageService;

        if (command.toString().startsWith("2")) {
            channelName = Constants.RabbitMQConstants.Im2GroupService;
        }

        try {
            channel = MqFactory.getChannel(channelName);

            JSONObject o = (JSONObject) JSON.toJSON(message.getMessagePack());
            o.put("command", command);
            o.put("clientType", message.getMessageHeader().getClientType());
            o.put("imei", message.getMessageHeader().getImei());
            o.put("appId", message.getMessageHeader().getAppId());
            channel.basicPublish(channelName, "", null, o.toJSONString().getBytes());
        } catch (Exception e) {
            log.error("发送消息出现异常：{}", e.getMessage());
        }
    }

}
