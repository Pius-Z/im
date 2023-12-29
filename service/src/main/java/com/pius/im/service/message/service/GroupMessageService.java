package com.pius.im.service.message.service;

import com.pius.im.codec.pack.message.ChatMessageAck;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.message.GroupChatMessageContent;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/28
 */
@Service
public class GroupMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    MessageStoreService messageStoreService;

    public void process(GroupChatMessageContent groupChatMessageContent) {

        String fromId = groupChatMessageContent.getFromId();
        String groupId = groupChatMessageContent.getGroupId();
        Integer appId = groupChatMessageContent.getAppId();

        // 前置校验
        ResponseVO responseVO = imServerPermissionCheck(fromId, groupId, appId);

        if (responseVO.isOk()) {
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupChatMessageContent.getGroupId(),
                    groupChatMessageContent.getAppId());
            groupChatMessageContent.setMemberId(groupMemberId);

            // 消息存储
            messageStoreService.storeGroupMessage(groupChatMessageContent);

            // 1.回ack成功给自己
            ack(groupChatMessageContent, responseVO);
            // 2.发消息给同步在线端
            syncToSender(groupChatMessageContent, groupChatMessageContent);
            // 3.发消息给对方在线端
            dispatchMessage(groupChatMessageContent);
        } else {
            // 通知发送端发送失败
            ack(groupChatMessageContent, responseVO);
        }
    }

    private ResponseVO imServerPermissionCheck(String fromId, String groupId, Integer appId) {
        return checkSendMessageService.checkGroupMessage(fromId, groupId, appId);
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        ResponseVO<ChatMessageAck> resp = new ResponseVO<>(responseVO.getCode(), responseVO.getMsg(), chatMessageAck);

        messageProducer.sendToUser(messageContent.getFromId(), GroupEventCommand.GROUP_MSG_ACK, resp, messageContent);
    }

    private void syncToSender(GroupChatMessageContent groupChatMessageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(groupChatMessageContent.getFromId(),
                GroupEventCommand.MSG_GROUP, groupChatMessageContent, clientInfo);
    }

    private void dispatchMessage(GroupChatMessageContent groupChatMessageContent) {
        for (String memberId : groupChatMessageContent.getMemberId()) {
            if (!memberId.equals(groupChatMessageContent.getFromId())) {
                messageProducer.sendToUser(memberId, GroupEventCommand.MSG_GROUP, groupChatMessageContent, groupChatMessageContent.getAppId());
            }
        }
    }
}
