package com.pius.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.proto.MessagePack;
import com.pius.im.common.constant.Constants;
import com.pius.im.tcp.receiver.process.BaseProcess;
import com.pius.im.tcp.receiver.process.ProcessFactory;
import com.pius.im.tcp.utils.MqFactory;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Pius
 * @Date: 2023/12/21
 */
@Slf4j
public class MessageReceiver {

    private static String brokerId;

    private static void startReceiverMessage() {
        try {
            Channel channel = MqFactory.getChannel(Constants.RabbitMQConstants.MessageService2Im + brokerId);
            channel.queueDeclare(Constants.RabbitMQConstants.MessageService2Im + brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitMQConstants.MessageService2Im + brokerId, Constants.RabbitMQConstants.MessageService2Im, brokerId);

            channel.basicConsume(Constants.RabbitMQConstants.MessageService2Im + brokerId, false,
                    new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            try {
                                String msgStr = new String(body);
                                log.info(msgStr);
                                MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                                BaseProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                                messageProcess.process(messagePack);
                                channel.basicAck(envelope.getDeliveryTag(), false);
                            } catch (Exception e) {
                                log.error(Arrays.toString(e.getStackTrace()));
                                channel.basicNack(envelope.getDeliveryTag(), false, false);
                            }
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

    public static void init(String brokerId) {
        if (StringUtils.isBlank(MessageReceiver.brokerId)) {
            MessageReceiver.brokerId = brokerId;
        }
        startReceiverMessage();
    }

}

