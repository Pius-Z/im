package com.pius.im.service.message.service;

import com.pius.im.codec.pack.message.MessageReadPack;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.common.model.message.MessageReceiveAckContent;
import com.pius.im.service.conversion.service.ConversationService;
import com.pius.im.service.utils.MessageProducer;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    ConversationService conversationService;

    public void receiveAck(MessageReceiveAckContent messageReceiveAckContent) {
        messageProducer.sendToUser(messageReceiveAckContent.getToId(),
                MessageCommand.MSG_RECEIVE_ACK, messageReceiveAckContent, messageReceiveAckContent.getAppId());
    }

    public void readMark(MessageReadContent messageReadContent) {
        conversationService.messageMarkRead(messageReadContent);
        MessageReadPack messageReadPack = new MessageReadPack();
        BeanUtils.copyProperties(messageReadContent, messageReadPack);
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadPack.getFromId(), MessageCommand.MSG_READ_SYNC,
                messageReadPack, messageReadContent);
        // 发送给对方
        messageProducer.sendToUser(messageReadContent.getToId(), MessageCommand.MSG_READ_RECEIPT,
                messageReadPack, messageReadContent.getAppId());
    }

    public void groupReadMark(MessageReadContent messageReadContent) {
        conversationService.messageMarkRead(messageReadContent);
        MessageReadPack messageReadPack = new MessageReadPack();
        BeanUtils.copyProperties(messageReadContent, messageReadPack);
        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(messageReadPack.getFromId(), GroupEventCommand.MSG_GROUP_READ_SYNC,
                messageReadPack, messageReadContent);
        // 发送给对方
        messageProducer.sendToUser(messageReadPack.getToId(), GroupEventCommand.MSG_GROUP_READ_RECEIPT,
                messageReadPack, messageReadContent.getAppId());
    }
}
