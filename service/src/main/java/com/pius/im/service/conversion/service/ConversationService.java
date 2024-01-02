package com.pius.im.service.conversion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.service.conversion.dao.ImConversationSetEntity;
import com.pius.im.service.conversion.dao.mapper.ImConversationSetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Service
public class ConversationService {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    public String convertConversationId(Integer type, String fromId, String toId) {
        return type + "_" + fromId + "_" + toId;
    }

    public void messageMarkRead(MessageReadContent messageReadContent) {

        String toId = messageReadContent.getToId();
        if (messageReadContent.getConversationType() == ConversationTypeEnum.GROUP.getCode()) {
            toId = messageReadContent.getGroupId();
        }
        String conversationId = convertConversationId(messageReadContent.getConversationType(),
                messageReadContent.getFromId(), toId);
        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        queryWrapper.eq("app_id", messageReadContent.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadContent, imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            imConversationSetMapper.insert(imConversationSetEntity);
        } else {
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetMapper.readMark(imConversationSetEntity);
        }
    }

}
