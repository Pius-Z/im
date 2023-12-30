package com.pius.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.model.message.StoreP2PMessageDto;
import com.pius.message.dao.ImMessageBodyEntity;
import com.pius.message.service.StoreMessageService;
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
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Slf4j
@Service
public class StoreP2PMessageReceiver {

    @Autowired
    StoreMessageService storeMessageService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = Constants.RabbitMQConstants.StoreP2PMessage, durable = "true"),
                    exchange = @Exchange(value = Constants.RabbitMQConstants.StoreP2PMessage)
            ), concurrency = "1"
    )
    public void onChatMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {
        String msg = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("STORE CHAT MSG FORM QUEUE ::: {}", msg);
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);
        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            StoreP2PMessageDto storeP2PMessageDto = jsonObject.toJavaObject(StoreP2PMessageDto.class);
            ImMessageBodyEntity imMessageBodyEntity = jsonObject.getObject("messageBody", ImMessageBodyEntity.class);
            storeMessageService.storeP2PMessage(storeP2PMessageDto.getMessageContent(), imMessageBodyEntity);
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
