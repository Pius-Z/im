package com.pius.im.tcp.receiver;

import com.pius.im.common.constant.Constants;
import com.pius.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Pius
 * @Date: 2023/12/21
 */
@Slf4j
public class MessageReceiver {

    private static void startReceiverMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitMQConstants.MessageService2Im);
            channel.queueDeclare(Constants.RabbitMQConstants.MessageService2Im, true, false, false, null);
            channel.queueBind(Constants.RabbitMQConstants.MessageService2Im, Constants.RabbitMQConstants.MessageService2Im, "");

            channel.basicConsume(Constants.RabbitMQConstants.MessageService2Im, false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            String msgStr = new String(body);
                            log.info(msgStr);
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }
                    }
            );
        } catch (IOException | TimeoutException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public static void init() {
        startReceiverMessage();
    }

}

