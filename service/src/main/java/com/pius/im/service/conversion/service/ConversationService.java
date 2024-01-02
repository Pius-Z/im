package com.pius.im.service.conversion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pius.im.codec.pack.conversation.DeleteConversationPack;
import com.pius.im.codec.pack.conversation.UpdateConversationPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.enums.ConversationErrorCode;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.command.ConversationEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.service.conversion.dao.ImConversationSetEntity;
import com.pius.im.service.conversion.dao.mapper.ImConversationSetMapper;
import com.pius.im.service.conversion.model.DeleteConversationReq;
import com.pius.im.service.conversion.model.UpdateConversationReq;
import com.pius.im.service.utils.MessageProducer;
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

    @Autowired
    AppConfig appConfig;

    @Autowired
    MessageProducer messageProducer;

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

    public ResponseVO deleteConversation(DeleteConversationReq req) {
        if (appConfig.isDeleteConversationSyncMode()) {
            DeleteConversationPack deleteConversationPack = new DeleteConversationPack();
            deleteConversationPack.setConversationId(req.getConversationId());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_DELETE,
                    deleteConversationPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO updateConversation(UpdateConversationReq req) {
        if (req.getIsTop() == null && req.getIsMute() == null) {
            return ResponseVO.errorResponse(ConversationErrorCode.CONVERSATION_UPDATE_PARAM_ERROR);
        }

        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", req.getConversationId());
        queryWrapper.eq("app_id", req.getAppId());
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity != null) {
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsTop(req.getIsTop());
            }
            if (req.getIsMute() != null) {
                imConversationSetEntity.setIsMute(req.getIsMute());
            }
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);

            UpdateConversationPack updateConversationPack = new UpdateConversationPack();
            updateConversationPack.setConversationId(req.getConversationId());
            updateConversationPack.setIsMute(imConversationSetEntity.getIsMute());
            updateConversationPack.setIsTop(imConversationSetEntity.getIsTop());
            updateConversationPack.setConversationType(imConversationSetEntity.getConversationType());
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_UPDATE,
                    updateConversationPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

}
