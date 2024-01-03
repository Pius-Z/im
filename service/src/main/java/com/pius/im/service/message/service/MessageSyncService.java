package com.pius.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.pius.im.codec.pack.message.MessageReadPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.SyncReq;
import com.pius.im.common.model.SyncResp;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.common.model.message.MessageReceiveAckContent;
import com.pius.im.common.model.message.OfflineMessageContent;
import com.pius.im.service.conversion.service.ConversationService;
import com.pius.im.service.utils.MessageProducer;
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
        //获取最大的seq
        Long maxSeq = 0L;
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        Set set = zSetOperations.reverseRangeWithScores(key, 0, 0);
        if(!CollectionUtils.isEmpty(set)){
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

        if(!CollectionUtils.isEmpty(respList)){
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq <= offlineMessageContent.getMessageKey());
        }

        return ResponseVO.successResponse(resp);
    }

}
