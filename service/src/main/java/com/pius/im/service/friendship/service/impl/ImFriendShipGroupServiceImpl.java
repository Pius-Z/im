package com.pius.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.pius.im.codec.pack.friendship.AddFriendGroupPack;
import com.pius.im.codec.pack.friendship.DeleteFriendGroupPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.enums.FriendShipErrorCode;
import com.pius.im.common.enums.command.FriendshipEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.pius.im.service.friendship.dao.mapper.ImFriendShipGroupMapper;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.pius.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.pius.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.pius.im.service.friendship.service.ImFriendShipGroupService;
import com.pius.im.service.seq.RedisSeq;
import com.pius.im.service.utils.MessageProducer;
import com.pius.im.service.utils.WriteUserSeq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@Slf4j
@Service
public class ImFriendShipGroupServiceImpl implements ImFriendShipGroupService {

    @Autowired
    ImFriendShipGroupMapper imFriendShipGroupMapper;

    @Lazy
    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    RedisSeq redisSeq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendShipGroupReq req) {

        QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("group_name", req.getGroupName());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(queryWrapper);

        long seq = 0L;
        if (entity != null) {
            // 添加的分组记录存在时
            // 若分组状态正常，返回分组已存在
            // 若分组状态为删除，更新分组状态为正常
            if (entity.getDelFlag() == DelFlagEnum.NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
            } else {
                seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup);
                ImFriendShipGroupEntity group = new ImFriendShipGroupEntity();
                group.setGroupId(entity.getGroupId());
                group.setUpdateTime(System.currentTimeMillis());
                group.setDelFlag(DelFlagEnum.NORMAL.getCode());
                group.setSequence(seq);

                int update = imFriendShipGroupMapper.updateById(group);
                if (update != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
                }
            }
        } else {
            // 添加的分组记录不存在时，添加新分组
            ImFriendShipGroupEntity group = new ImFriendShipGroupEntity();
            group.setAppId(req.getAppId());
            group.setFromId(req.getFromId());
            group.setGroupName(req.getGroupName());
            group.setCreateTime(System.currentTimeMillis());
            group.setDelFlag(DelFlagEnum.NORMAL.getCode());
            group.setSequence(seq);

            try {
                int insert = imFriendShipGroupMapper.insert(group);
                if (insert != 1) {
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_CREATE_ERROR);
                }
            } catch (DuplicateKeyException e) {
                log.error(e.getMessage());
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
            }

        }

        AddFriendGroupPack addFriendGroupPack = new AddFriendGroupPack();
        addFriendGroupPack.setFromId(req.getFromId());
        addFriendGroupPack.setGroupName(req.getGroupName());
        addFriendGroupPack.setSequence(seq);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_ADD,
                addFriendGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        // 添加分组成员
        if (CollectionUtils.isNotEmpty(req.getToIds())) {

            AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
            addFriendShipGroupMemberReq.setAppId(req.getAppId());
            addFriendShipGroupMemberReq.setFromId(req.getFromId());
            addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
            addFriendShipGroupMemberReq.setToIds(req.getToIds());

            return imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
        }

        writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req) {

        for (String groupName : req.getGroupName()) {
            QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.eq("from_id", req.getFromId());
            queryWrapper.eq("group_name", groupName);
            queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

            ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(queryWrapper);

            if (entity != null) {
                long seq = redisSeq.doGetSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup);

                ImFriendShipGroupEntity update = new ImFriendShipGroupEntity();
                update.setGroupId(entity.getGroupId());
                update.setDelFlag(DelFlagEnum.DELETE.getCode());
                update.setSequence(seq);
                imFriendShipGroupMapper.updateById(update);
                imFriendShipGroupMemberService.clearGroupMember(entity.getGroupId());

                DeleteFriendGroupPack deleteFriendGroupPack = new DeleteFriendGroupPack();
                deleteFriendGroupPack.setFromId(req.getFromId());
                deleteFriendGroupPack.setGroupName(groupName);
                deleteFriendGroupPack.setSequence(seq);
                messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_DELETE,
                        deleteFriendGroupPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

                writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipGroup, seq);
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId) {

        QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("group_name", groupName);
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(queryWrapper);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(entity);
    }

    @Override
    public Long updateSeq(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_name", groupName);
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(queryWrapper);

        long seq = redisSeq.doGetSeq(appId + ":" + Constants.SeqConstants.FriendshipGroup);

        ImFriendShipGroupEntity group = new ImFriendShipGroupEntity();
        group.setGroupId(entity.getGroupId());
        group.setSequence(seq);
        imFriendShipGroupMapper.updateById(group);
        writeUserSeq.writeUserSeq(appId, fromId, Constants.SeqConstants.FriendshipGroup, seq);

        return seq;
    }

}
