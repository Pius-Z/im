package com.pius.im.service.message.service;

import com.pius.im.codec.pack.message.ChatMessageAck;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.message.GroupChatMessageContent;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.common.model.message.OfflineMessageContent;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.message.model.req.SendGroupMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.seq.RedisSeq;
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
 * @Date: 2023/12/28
 */
@Slf4j
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

    public void process(GroupChatMessageContent groupChatMessageContent) {

        // 从缓存中获取消息
        GroupChatMessageContent messageFromMessageIdCache = messageStoreService.getMessageFromMessageIdCache(groupChatMessageContent.getAppId(),
                groupChatMessageContent.getMessageId(), GroupChatMessageContent.class);
        if (messageFromMessageIdCache != null) {
            threadPoolExecutor.execute(() -> {
                // 1.回ack成功给自己
                ack(messageFromMessageIdCache, ResponseVO.successResponse());
                // 2.发消息给同步在线端
                syncToSender(messageFromMessageIdCache, messageFromMessageIdCache);
                // 3.发消息给对方在线端
                dispatchMessage(messageFromMessageIdCache);
            });
        }

        threadPoolExecutor.execute(() -> {

            long seq = redisSeq.doGetSeq(groupChatMessageContent.getAppId() + ":" + Constants.SeqConstants.GroupMessage
                    + groupChatMessageContent.getGroupId());
            groupChatMessageContent.setMessageSequence(seq);
            // 消息存储
            messageStoreService.storeGroupMessage(groupChatMessageContent);

            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(groupChatMessageContent.getGroupId(),
                    groupChatMessageContent.getAppId());
            groupChatMessageContent.setMemberId(groupMemberId);
            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(groupChatMessageContent, offlineMessageContent);
            offlineMessageContent.setToId(groupChatMessageContent.getGroupId());
            offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
            messageStoreService.storeGroupOfflineMessage(offlineMessageContent, groupMemberId);

            // 1.回ack成功给自己
            ack(groupChatMessageContent, ResponseVO.successResponse());
            // 2.发消息给同步在线端
            syncToSender(groupChatMessageContent, groupChatMessageContent);
            // 3.发消息给对方在线端
            dispatchMessage(groupChatMessageContent);

            // 将message存到缓存中
            messageStoreService.setMessageFromMessageIdCache(groupChatMessageContent.getAppId(),
                    groupChatMessageContent.getMessageId(), groupChatMessageContent);
        });
    }

    public ResponseVO imServerPermissionCheck(String fromId, String groupId, Integer appId) {
        return checkSendMessageService.checkGroupMessage(fromId, groupId, appId);
    }

    private void ack(MessageContent messageContent, ResponseVO responseVO) {

        ChatMessageAck chatMessageAck = new ChatMessageAck(messageContent.getMessageId(), messageContent.getMessageSequence());
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

    public ResponseVO<SendMessageResp> send(SendGroupMessageReq sendGroupMessageReq) {

        GroupChatMessageContent groupChatMessageContent = new GroupChatMessageContent();
        BeanUtils.copyProperties(sendGroupMessageReq, groupChatMessageContent);

        // 前置校验
        ResponseVO responseVO = imServerPermissionCheck(groupChatMessageContent.getFromId(), groupChatMessageContent.getGroupId(),
                groupChatMessageContent.getAppId());

        if (!responseVO.isOk()) {
            return ResponseVO.errorResponse(responseVO.getCode(), responseVO.getMsg());
        }

        // 保存消息
        messageStoreService.storeGroupMessage(groupChatMessageContent);

        SendMessageResp sendMessageResp = new SendMessageResp();
        sendMessageResp.setMessageKey(groupChatMessageContent.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        // 发消息给同步在线端
        syncToSender(groupChatMessageContent, groupChatMessageContent);

        // 发消息给对方在线端
        dispatchMessage(groupChatMessageContent);

        return ResponseVO.successResponse(sendMessageResp);
    }

}
