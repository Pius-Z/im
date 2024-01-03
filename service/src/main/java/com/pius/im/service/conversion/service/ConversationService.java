package com.pius.im.service.conversion.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.pius.im.codec.pack.conversation.DeleteConversationPack;
import com.pius.im.codec.pack.conversation.UpdateConversationPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.ConversationErrorCode;
import com.pius.im.common.enums.ConversationTypeEnum;
import com.pius.im.common.enums.command.ConversationEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.common.model.SyncReq;
import com.pius.im.common.model.SyncResp;
import com.pius.im.common.model.message.MessageReadContent;
import com.pius.im.service.conversion.dao.ImConversationSetEntity;
import com.pius.im.service.conversion.dao.mapper.ImConversationSetMapper;
import com.pius.im.service.conversion.model.DeleteConversationReq;
import com.pius.im.service.conversion.model.UpdateConversationReq;
import com.pius.im.service.seq.RedisSeq;
import com.pius.im.service.utils.MessageProducer;
import com.pius.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

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
        long seq;
        ImConversationSetEntity imConversationSetEntity = imConversationSetMapper.selectOne(queryWrapper);
        if (imConversationSetEntity == null) {
            imConversationSetEntity = new ImConversationSetEntity();
            imConversationSetEntity.setConversationId(conversationId);
            BeanUtils.copyProperties(messageReadContent, imConversationSetEntity);
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            imConversationSetEntity.setToId(toId);
            seq = redisSeq.doGetSeq(messageReadContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.insert(imConversationSetEntity);
        } else {
            imConversationSetEntity.setReadSequence(messageReadContent.getMessageSequence());
            seq = redisSeq.doGetSeq(messageReadContent.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.readMark(imConversationSetEntity);
        }
        writeUserSeq.writeUserSeq(messageReadContent.getAppId(), messageReadContent.getFromId(), Constants.SeqConstants.Conversation, seq);
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
            long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.Conversation);
            imConversationSetEntity.setSequence(seq);
            imConversationSetMapper.update(imConversationSetEntity, queryWrapper);
            writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.Conversation, seq);

            UpdateConversationPack updateConversationPack = new UpdateConversationPack();
            updateConversationPack.setConversationId(req.getConversationId());
            updateConversationPack.setIsMute(imConversationSetEntity.getIsMute());
            updateConversationPack.setIsTop(imConversationSetEntity.getIsTop());
            updateConversationPack.setConversationType(imConversationSetEntity.getConversationType());
            updateConversationPack.setSequence(seq);
            messageProducer.sendToUserExceptClient(req.getFromId(), ConversationEventCommand.CONVERSATION_UPDATE,
                    updateConversationPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));
        }

        return ResponseVO.successResponse();
    }

    public ResponseVO<SyncResp<ImConversationSetEntity>> syncConversationSet(SyncReq req) {
        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        QueryWrapper<ImConversationSetEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getOperator());
        queryWrapper.gt("sequence", req.getLastSequence());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.last(" limit " + req.getMaxLimit());
        queryWrapper.orderByAsc("sequence");
        List<ImConversationSetEntity> list = imConversationSetMapper.selectList(queryWrapper);

        SyncResp<ImConversationSetEntity> resp = new SyncResp<>();
        // 设置最大seq
        Long friendShipMaxSeq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(), req.getOperator());
        resp.setMaxSequence(friendShipMaxSeq);

        if (!CollectionUtils.isEmpty(list)) {
            resp.setDataList(list);
            // 设置是否拉取完毕
            ImConversationSetEntity maxSeqEntity = list.get(list.size() - 1);
            resp.setCompleted(maxSeqEntity.getSequence() >= friendShipMaxSeq);
        } else {
            resp.setCompleted(true);
        }

        return ResponseVO.successResponse(resp);
    }

}
