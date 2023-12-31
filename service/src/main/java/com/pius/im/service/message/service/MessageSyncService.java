package com.pius.im.service.message.service;

import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.message.MessageReceiveAckContent;
import com.pius.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Pius
 * @Date: 2023/12/31
 */
@Service
public class MessageSyncService {

    @Autowired
    MessageProducer messageProducer;

    public void receiveAck(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }
}
