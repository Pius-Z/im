package com.pius.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.model.message.*;
import com.pius.im.service.utils.SnowflakeIdWorker;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

}
