package com.pius.message.service;

import com.pius.im.common.model.message.GroupChatMessageContent;
import com.pius.im.common.model.message.MessageContent;
import com.pius.message.dao.ImGroupMessageHistoryEntity;
import com.pius.message.dao.ImMessageBodyEntity;
import com.pius.message.dao.ImMessageHistoryEntity;
import com.pius.message.dao.mapper.ImGroupMessageHistoryMapper;
import com.pius.message.dao.mapper.ImMessageBodyMapper;
import com.pius.message.dao.mapper.ImMessageHistoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Service
public class StoreMessageService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    @Transactional
    public void storeP2PMessage(MessageContent messageContent, ImMessageBodyEntity imMessageBodyEntity) {
        imMessageBodyMapper.insert(imMessageBodyEntity);
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(messageContent, imMessageBodyEntity);
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(MessageContent messageContent,
                                                                   ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, fromHistory);
        fromHistory.setOwnerId(messageContent.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setCreateTime(System.currentTimeMillis());
        fromHistory.setSequence(messageContent.getMessageSequence());

        ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, toHistory);
        toHistory.setOwnerId(messageContent.getToId());
        toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        toHistory.setCreateTime(System.currentTimeMillis());
        toHistory.setSequence(messageContent.getMessageSequence());

        list.add(fromHistory);
        list.add(toHistory);

        return list;
    }

    @Transactional
    public void storeGroupMessage(GroupChatMessageContent groupChatMessageContent, ImMessageBodyEntity imMessageBodyEntity) {
        imMessageBodyMapper.insert(imMessageBodyEntity);
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(groupChatMessageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
    }

    private ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent messageContent,
                                                                     ImMessageBodyEntity messageBodyEntity) {
        ImGroupMessageHistoryEntity result = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(messageContent, result);
        result.setGroupId(messageContent.getGroupId());
        result.setMessageKey(messageBodyEntity.getMessageKey());
        result.setCreateTime(System.currentTimeMillis());
        result.setSequence(messageContent.getMessageSequence());

        return result;
    }
}
