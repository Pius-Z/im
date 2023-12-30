package com.pius.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.model.message.*;
import com.pius.im.service.utils.SnowflakeIdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
