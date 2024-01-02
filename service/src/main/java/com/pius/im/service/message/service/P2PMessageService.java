package com.pius.im.service.message.service;

import com.pius.im.codec.pack.message.ChatMessageAck;
import com.pius.im.codec.pack.message.MessageReceiveServerAck;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.common.model.message.OfflineMessageContent;
import com.pius.im.service.message.model.req.SendMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.seq.RedisSeq;
import com.pius.im.service.utils.ConversationIdGenerate;
import com.pius.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Autowired
    RedisSeq redisSeq;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(1000), r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName("message-process-thread-" + num.getAndIncrement());
            return thread;
        });
    }

    public void process(MessageContent messageContent) {

        // 从缓存中获取消息
        MessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(
                messageContent.getAppId(), messageContent.getMessageId(), messageContent.getClass());
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                // 1.返回给发送端ack
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                // 2.发消息给同步在线端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 3.发消息给对方在线端
                List<ClientInfo> clientInfos = dispatchMessage(messageFromMessageIdCache);
                if (clientInfos.isEmpty()) {
                    // 发送接收确认给发送方，要带上是服务端发送的标识
                    receiveAck(messageFromMessageIdCache);
                }
            });
            return;
        }

        threadPoolExecutor.execute(() -> {

            long seq = redisSeq.doGetSeq(messageContent.getAppId() + ":" + Constants.SeqConstants.Message
                    + ":" + ConversationIdGenerate.generateP2PId(messageContent.getFromId(), messageContent.getToId()));
            messageContent.setMessageSequence(seq);

            messageStoreService.storeP2PMessage(messageContent);

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(messageContent, offlineMessageContent);
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            messageStoreService.storeOfflineMessage(offlineMessageContent);

            // 1.返回给发送端ack
            ack(messageContent, ResponseVO.successResponse());
            // 2.发消息给同步在线端
            syncToSender(messageContent, messageContent);
            // 3.发消息给对方在线端
            List<ClientInfo> clientInfos = dispatchMessage(messageContent);
            if (clientInfos.isEmpty()) {
                // 发送接收确认给发送方，要带上是服务端发送的标识
                receiveAck(messageContent);
            }

            // 将message存到缓存中
            messageStoreService.setMessageFromMessageIdCache(messageContent.getAppId(),
                    messageContent.getMessageId(), messageContent);
        });
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

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(), messageContent.getMessageSequence());
        ResponseVO<ChatMessageAck> resp = new ResponseVO<>(responseVO.getCode(), responseVO.getMsg(), chatMessageAck);

        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_ACK, resp, messageContent);
    }

    private void syncToSender(MessageContent messageContent, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(messageContent.getFromId(), MessageCommand.MSG_P2P, messageContent, clientInfo);
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent) {
        return messageProducer.sendToUser(messageContent.getToId(), MessageCommand.MSG_P2P, messageContent, messageContent.getAppId());
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

    public void receiveAck(MessageContent messageContent) {
        MessageReceiveServerAck messageReceiveServerAck = new MessageReceiveServerAck();
        messageReceiveServerAck.setFromId(messageContent.getToId());
        messageReceiveServerAck.setToId(messageContent.getFromId());
        messageReceiveServerAck.setMessageKey(messageContent.getMessageKey());
        messageReceiveServerAck.setMessageSequence(messageContent.getMessageSequence());
        messageReceiveServerAck.setServerSend(true);
        messageProducer.sendToUser(messageContent.getFromId(), MessageCommand.MSG_RECEIVE_ACK,
                messageReceiveServerAck, new ClientInfo(messageContent.getAppId(),
                        messageContent.getClientType(), messageContent.getImei()));
    }

}
