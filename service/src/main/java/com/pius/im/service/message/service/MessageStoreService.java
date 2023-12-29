package com.pius.im.service.message.service;

import com.pius.im.common.config.AppConfig;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.model.message.GroupChatMessageContent;
import com.pius.im.common.model.message.MessageContent;
import com.pius.im.service.message.dao.ImGroupMessageHistoryEntity;
import com.pius.im.service.message.dao.ImMessageBodyEntity;
import com.pius.im.service.message.dao.ImMessageHistoryEntity;
import com.pius.im.service.message.dao.mapper.ImGroupMessageHistoryMapper;
import com.pius.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.pius.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.pius.im.service.utils.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
@Service
public class MessageStoreService {
    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    AppConfig appConfig;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent) {

        // messageContent 转化成 messageBody
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        // 插入messageBody
        imMessageBodyMapper.insert(imMessageBodyEntity);

        // 写扩散
        // 转化成MessageHistory
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
        // 批量插入
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);

        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());

    }

    public ImMessageBodyEntity extractMessageBody(MessageContent messageContent) {

        ImMessageBodyEntity imMessageBodyEntity = new ImMessageBodyEntity();
        imMessageBodyEntity.setAppId(messageContent.getAppId());
        imMessageBodyEntity.setMessageKey(snowflakeIdWorker.nextId());
        imMessageBodyEntity.setCreateTime(System.currentTimeMillis());
        imMessageBodyEntity.setSecurityKey("");
        imMessageBodyEntity.setExtra(messageContent.getExtra());
        imMessageBodyEntity.setDelFlag(DelFlagEnum.NORMAL.getCode());
        imMessageBodyEntity.setMessageTime(messageContent.getMessageTime());
        imMessageBodyEntity.setMessageBody(messageContent.getMessageBody());

        return imMessageBodyEntity;
    }


    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());

        list.add(fromHistory);
        list.add(toHistory);

        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent messageContent) {
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(messageContent);
        imMessageBodyMapper.insert((imMessageBodyEntity));

        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(messageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);

        messageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent,
                                                                     ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, imGroupMessageHistoryEntity);
        imGroupMessageHistoryEntity.setGroupId(messageContent.getGroupId());
        imGroupMessageHistoryEntity.setMessageKey(messageBodyEntity.getMessageKey());
        imGroupMessageHistoryEntity.setCreateTime(System.currentTimeMillis());

        return imGroupMessageHistoryEntity;
    }

}
