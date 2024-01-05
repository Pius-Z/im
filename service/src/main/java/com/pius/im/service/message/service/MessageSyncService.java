package com.pius.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.pius.im.codec.pack.message.MessageReadPack;
import com.pius.im.codec.pack.message.RecallMessageNotifyPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.enums.MessageErrorCode;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.SyncReq;
import com.pius.im.common.model.SyncResp;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.common.model.message.MessageReceiveAckContent;
import com.pius.im.common.model.message.OfflineMessageContent;
import com.pius.im.common.model.message.RecallMessageContent;
import com.pius.im.service.conversion.service.ConversationService;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.message.dao.ImMessageBodyEntity;
import com.pius.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.pius.im.service.seq.RedisSeq;
import com.pius.im.service.utils.ConversationIdGenerate;
import com.pius.im.service.utils.GroupMessageProducer;
import com.pius.im.service.utils.MessageProducer;
import com.pius.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

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

    public ResponseVO<SyncResp<OfflineMessageContent>> syncOfflineMessage(SyncReq req) {

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        String key = req.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + req.getOperator();
        // 获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if (!CollectionUtils.isEmpty(set)) {
            List list = new ArrayList(set);
            DefaultTypedTuple o = (DefaultTypedTuple) list.get(0);
            maxSeq = o.getScore().longValue();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();
        resp.setMaxSequence(maxSeq);

        Set<ZSetOperations.TypedTuple> querySet = zSetOperations.rangeByScoreWithScores(key,
                req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple<String> typedTuple : querySet) {
            String value = typedTuple.getValue();
            OfflineMessageContent offlineMessageContent = JSONObject.parseObject(value, OfflineMessageContent.class);
            respList.add(offlineMessageContent);
        }
        resp.setDataList(respList);

        if (!CollectionUtils.isEmpty(respList)) {
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

    public void recallMessage(RecallMessageContent content) {

        Long messageTime = content.getMessageTime();
        Long now = System.currentTimeMillis();

        RecallMessageNotifyPack pack = new RecallMessageNotifyPack();
        BeanUtils.copyProperties(content, pack);

        if (12000000L < now - messageTime) {
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_RECALL_TIME_OUT), content);
            return;
        }

        QueryWrapper<ImMessageBodyEntity> query = new QueryWrapper<>();
        query.eq("app_id", content.getAppId());
        query.eq("message_key", content.getMessageKey());
        ImMessageBodyEntity body = imMessageBodyMapper.selectOne(query);

        if (body == null) {
            // 不存在的消息不能撤回
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_BODY_IS_NOT_EXIST), content);
            return;
        }

        if (body.getDelFlag() == DelFlagEnum.DELETE.getCode()) {
            // 已经撤回的不能再撤回
            recallAck(pack, ResponseVO.errorResponse(MessageErrorCode.MESSAGE_IS_RECALLED), content);
            return;
        }

        // 更新消息状态
        body.setDelFlag(DelFlagEnum.DELETE.getCode());
        imMessageBodyMapper.update(body, query);

        if (content.getConversationType() == ConversationTypeEnum.P2P.getCode()) {
            // 找到fromId的队列
            String fromKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getFromId();
            // 找到toId的队列
            String toKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + content.getToId();

            OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
            BeanUtils.copyProperties(content, offlineMessageContent);
            offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
            offlineMessageContent.setMessageKey(content.getMessageKey());
            offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType(),
                    content.getFromId(), content.getToId()));
            offlineMessageContent.setMessageBody(body.getMessageBody());
            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message + ":"
                    + ConversationIdGenerate.generateP2PId(content.getFromId(), content.getToId()));
            offlineMessageContent.setMessageSequence(seq);

            long messageKey = SnowflakeIdWorker.nextId();
            redisTemplate.opsForZSet().add(fromKey, JSONObject.toJSONString(offlineMessageContent), messageKey);

            offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType(),
                    content.getToId(), content.getToId()));
            redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), messageKey);

            // ack
            recallAck(pack, ResponseVO.successResponse(), content);
            // 分发给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack, content);
            // 分发给对方
            messageProducer.sendToUser(content.getToId(), MessageCommand.MSG_RECALL_NOTIFY,
                    pack, content.getAppId());
        } else {
            List<String> groupMemberId = imGroupMemberService.getGroupMemberId(content.getToId(), content.getAppId());
            long seq = redisSeq.doGetSeq(content.getAppId() + ":" + Constants.SeqConstants.Message + ":" + ConversationIdGenerate.generateP2PId(content.getFromId(), content.getToId()));
            // ack
            recallAck(pack, ResponseVO.successResponse(), content);
            // 发送给同步端
            messageProducer.sendToUserExceptClient(content.getFromId(), MessageCommand.MSG_RECALL_NOTIFY, pack, content);
            for (String memberId : groupMemberId) {
                if (memberId.equals(content.getFromId())) {
                    continue;
                }

                String toKey = content.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + memberId;
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(content, offlineMessageContent);
                offlineMessageContent.setDelFlag(DelFlagEnum.DELETE.getCode());
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType()
                        , memberId, content.getToId()));
                offlineMessageContent.setMessageBody(body.getMessageBody());
                offlineMessageContent.setMessageSequence(seq);
                long messageKey = SnowflakeIdWorker.nextId();
                redisTemplate.opsForZSet().add(toKey, JSONObject.toJSONString(offlineMessageContent), messageKey);

                // 分发给群成员
                messageProducer.sendToUser(memberId, MessageCommand.MSG_RECALL_NOTIFY,
                        pack, content.getAppId());
            }
        }
    }

    private void recallAck(RecallMessageNotifyPack recallPack, ResponseVO<Object> resp, ClientInfo clientInfo) {
        messageProducer.sendToUser(recallPack.getFromId(), MessageCommand.MSG_RECALL_ACK, resp, clientInfo);
    }

}
