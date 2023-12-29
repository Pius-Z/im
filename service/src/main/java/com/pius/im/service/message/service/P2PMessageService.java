package com.pius.im.service.message.service;

import com.pius.im.codec.pack.message.ChatMessageAck;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.service.message.model.req.SendMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Slf4j
@Service
public class P2PMessageService {

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageStoreService messageStoreService;

    public void process(MessageContent messageContent) {

        Integer appId = messageContent.getAppId();
        String fromId = messageContent.getFromId();
        String toId = messageContent.getToId();
        ResponseVO responseVO = imServerPermissionCheck(fromId, toId, appId);
        if (responseVO.isOk()) {

            messageStoreService.storeP2PMessage(messageContent);

            // 1.返回给发送端ack
            ack(messageContent, ResponseVO.successResponse());
            // 2.发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3.发消息给对方在线端
            dispatchMessage(messageContent);
        } else {
            // 通知发送端发送失败
            ack(messageContent, responseVO);
        }

    }

    public ResponseVO imServerPermissionCheck(String fromId, String toId, Integer appId) {
        ResponseVO responseVO = checkSendMessageService.checkSenderForbiddenOrMute(fromId, appId);
        if (!responseVO.isOk()) {
            return responseVO;
        }

        return checkSendMessageService.checkFriendShip(fromId, toId, appId);
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {
        log.info("msg ack,msgId={},checkResult{}", messageContent.getMessageId(), responseVO.getCode());

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId());
        ResponseVO<ChatMessageAck> resp = new ResponseVO<>(responseVO.getCode(), responseVO.getMsg(), chatMessageAck);

        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, resp, messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private void dispatchMessage(MessageContent messageContent) {
        messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
    }

    public ResponseVO<SendMessageResp> send(SendMessageReq sendMessageReq) {

        MessageContent messageContent = new MessageContent();
        BeanUtils.copyProperties(sendMessageReq, messageContent);

        // 前置校验
        ResponseVO responseVO = imServerPermissionCheck(messageContent.getFromId(), messageContent.getToId(),
                messageContent.getAppId());

        if (!responseVO.isOk()) {
            return ResponseVO.errorResponse(responseVO.getCode(), responseVO.getMsg());
        }

        // 保存消息
        messageStoreService.storeP2PMessage(messageContent);

        SendMessageResp sendMessageResp = new SendMessageResp();
        sendMessageResp.setMessageKey(messageContent.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        // 发消息给同步在线端
        syncToSender(messageContent, messageContent);

        // 发消息给对方在线端
        dispatchMessage(messageContent);

        return ResponseVO.successResponse(sendMessageResp);
    }

}
