package com.pius.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.model.message.*;
import com.pius.im.service.conversion.service.ConversationService;
import com.pius.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
@Service
public class MessageStoreService {

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    AppConfig appConfig;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ConversationService conversationService;

    public void storeP2PMessage(MessageContent messageContent) {
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        StoreP2PMessageDto storeP2PMessageDto = new StoreP2PMessageDto();
        storeP2PMessageDto.setMessageContent(messageContent);
        storeP2PMessageDto.setMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitMQConstants.StoreP2PMessage, "",
                JSONObject.toJSONString(storeP2PMessageDto));
    }

    public void storeGroupMessage(GroupChatMessageContent messageContent) {
        ImMessageBody imMessageBody = extractMessageBody(messageContent);
        StoreGroupMessageDto storeGroupMessageDto = new StoreGroupMessageDto();
        storeGroupMessageDto.setGroupChatMessageContent(messageContent);
        storeGroupMessageDto.setMessageBody(imMessageBody);
        messageContent.setMessageKey(imMessageBody.getMessageKey());
        rabbitTemplate.convertAndSend(Constants.RabbitMQConstants.StoreGroupMessage, "",
                JSONObject.toJSONString(storeGroupMessageDto));
    }

    public ImMessageBody extractMessageBody(MessageContent messageContent) {

        ImMessageBody imMessageBody = new ImMessageBody();
        imMessageBody.setAppId(messageContent.getAppId());
        imMessageBody.setMessageKey(snowflakeIdWorker.nextId());
        imMessageBody.setCreateTime(System.currentTimeMillis());
        imMessageBody.setSecurityKey("");
        imMessageBody.setExtra(messageContent.getExtra());
        imMessageBody.setDelFlag(DelFlagEnum.NORMAL.getCode());
        imMessageBody.setMessageTime(messageContent.getMessageTime());
        imMessageBody.setMessageBody(messageContent.getMessageBody());

        return imMessageBody;
    }

    public void setMessageFromMessageIdCache(Integer appId, String messageId, Object messageContent) {
        // appId : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(messageContent), 60, TimeUnit.SECONDS);
    }

    public <T> T getMessageFromMessageIdCache(Integer appId, String messageId, Class<T> clazz) {
        // appId : cache : messageId
        String key = appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId;
        String msg = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        return JSONObject.parseObject(msg, clazz);
    }

    public void storeOfflineMessage(OfflineMessageContent offlineMessage) {
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();

        // 生成发送者的的离线消息集合键
        String fromKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getFromId();
        // 判断离线消息数量是否超过设定值
        if (operations.zCard(fromKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(fromKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.P2P.getCode(),
                offlineMessage.getFromId(), offlineMessage.getToId()));
        // 插入数据 以messageKey作为分值
        operations.add(fromKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());

        // 生成接收者的的离线消息集合键
        String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + offlineMessage.getToId();
        // 判断离线消息数量是否超过设定值
        if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
            operations.removeRange(toKey, 0, 0);
        }
        offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.P2P.getCode(),
                offlineMessage.getToId(), offlineMessage.getFromId()));
        // 插入数据 以messageKey作为分值
        operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
    }

    public void storeGroupOfflineMessage(OfflineMessageContent offlineMessage, List<String> memberIds) {
        ZSetOperations<String, String> operations = stringRedisTemplate.opsForZSet();
        for (String memberId : memberIds) {
            // 生成接收者的的离线消息集合键
            String toKey = offlineMessage.getAppId() + ":" + Constants.RedisConstants.OfflineMessage + ":" + memberId;
            // 判断离线消息数量是否超过设定值
            if (operations.zCard(toKey) > appConfig.getOfflineMessageCount()) {
                operations.removeRange(toKey, 0, 0);
            }
            offlineMessage.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.GROUP.getCode(),
                    memberId, offlineMessage.getToId()));
            // 插入数据 以messageKey作为分值
            operations.add(toKey, JSONObject.toJSONString(offlineMessage), offlineMessage.getMessageKey());
        }
    }

}
