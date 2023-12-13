package com.pius.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.enums.FriendShipErrorCode;
import com.pius.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.pius.im.service.friendship.dao.mapper.ImFriendShipGroupMapper;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.pius.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.pius.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.pius.im.service.friendship.service.ImFriendShipGroupService;
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

    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendShipGroupReq req) {

        QueryWrapper<ImFriendShipGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("group_name", req.getGroupName());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(queryWrapper);

        if (entity != null) {
            // 添加的分组记录存在时
            // 若分组状态正常，返回分组已存在
            // 若分组状态为删除，更新分组状态为正常
            if (entity.getDelFlag() == DelFlagEnum.NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
            } else {
                ImFriendShipGroupEntity group = new ImFriendShipGroupEntity();
                group.setGroupId(entity.getGroupId());
                group.setUpdateTime(System.currentTimeMillis());
                group.setDelFlag(DelFlagEnum.NORMAL.getCode());

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

        // 添加分组成员
        if (CollectionUtils.isNotEmpty(req.getToIds())) {

            AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
            addFriendShipGroupMemberReq.setAppId(req.getAppId());
            addFriendShipGroupMemberReq.setFromId(req.getFromId());
            addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
            addFriendShipGroupMemberReq.setToIds(req.getToIds());

            return imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
        }

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
                ImFriendShipGroupEntity update = new ImFriendShipGroupEntity();
                update.setGroupId(entity.getGroupId());
                update.setDelFlag(DelFlagEnum.DELETE.getCode());
                imFriendShipGroupMapper.updateById(update);
                imFriendShipGroupMemberService.clearGroupMember(entity.getGroupId());
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
}
