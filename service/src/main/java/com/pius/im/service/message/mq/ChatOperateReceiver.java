package com.pius.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.common.model.message.MessageReceiveAckContent;
import com.pius.im.common.model.message.RecallMessageContent;
import com.pius.im.service.message.service.MessageSyncService;
import com.pius.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Slf4j
@Component
public class ChatOperateReceiver {

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitMQConstants.Im2MessageService, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitMQConstants.Im2MessageService)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            Integer command = jsonObject.getInteger("command");
            if (command.equals(MessageCommand.MSG_P2P.getCommand())) {
                // 处理消息
                MessageContent messageContent = jsonObject.toJavaObject(MessageContent.class);
                p2PMessageService.process(messageContent);
            } else if (command.equals(MessageCommand.MSG_RECEIVE_ACK.getCommand())) {
                // 消息接收确认
                MessageReceiveAckContent messageReceiveAckContent = jsonObject.toJavaObject(MessageReceiveAckContent.class);
                messageSyncService.receiveAck(messageReceiveAckContent);
            } else if (command.equals(MessageCommand.MSG_READ.getCommand())) {
                // 消息已读
                MessageReadContent messageReadContent = jsonObject.toJavaObject(MessageReadContent.class);
                messageSyncService.readMark(messageReadContent);
            } else if (Objects.equals(command, MessageCommand.MSG_RECALL.getCommand())) {
                // 撤回消息
                RecallMessageContent messageContent = jsonObject.toJavaObject(RecallMessageContent.class);
                messageSyncService.recallMessage(messageContent);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息出现异常：{}", e.getMessage());
            log.error("RMQ_CHAT_TRAN_ERROR", e);
            log.error("NACK_MSG:{}", msg);
            // 第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }
    }

}
