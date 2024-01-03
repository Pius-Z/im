package com.pius.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pius.im.codec.pack.friendship.AddFriendGroupMemberPack;
import com.pius.im.codec.pack.friendship.DeleteFriendGroupMemberPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.command.FriendshipEventCommand;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.pius.im.service.friendship.dao.ImFriendShipGroupMemberEntity;
import com.pius.im.service.friendship.dao.mapper.ImFriendShipGroupMemberMapper;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.pius.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;
import com.pius.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.pius.im.service.friendship.service.ImFriendShipGroupService;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.service.ImUserService;
import com.pius.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@Slf4j
@Service
public class ImFriendShipGroupMemberServiceImpl implements ImFriendShipGroupMemberService {

    @Autowired
    ImFriendShipGroupMemberMapper imFriendShipGroupMemberMapper;

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImUserService imUserService;

    @Autowired
    MessageProducer messageProducer;

    @Override
    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req) {

        ResponseVO<ImFriendShipGroupEntity> groupInfo = imFriendShipGroupService
                .getGroup(req.getFromId(), req.getGroupName(), req.getAppId());
        if (!groupInfo.isOk()) {
            return groupInfo;
        }

        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> userInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (userInfo.isOk()) {
                int add = doAddGroupMember(groupInfo.getData().getGroupId(), toId);
                if (add == 1) {
                    successId.add(toId);
                }
            }
        }

        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());

        AddFriendGroupMemberPack addFriendGroupMemberPack = new AddFriendGroupMemberPack();
        addFriendGroupMemberPack.setFromId(req.getFromId());
        addFriendGroupMemberPack.setGroupName(req.getGroupName());
        addFriendGroupMemberPack.setToIds(successId);
        addFriendGroupMemberPack.setSequence(seq);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_ADD,
                addFriendGroupMemberPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(successId);
    }

    public int doAddGroupMember(Long groupId, String toId) {
        // 先查询是否有成员，若有则添加否则不做操作
        QueryWrapper<ImFriendShipGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("to_id", toId);
        ImFriendShipGroupMemberEntity member = imFriendShipGroupMemberMapper.selectOne(queryWrapper);
        if (member != null) {
            return 1;
        }

        ImFriendShipGroupMemberEntity entity = new ImFriendShipGroupMemberEntity();
        entity.setGroupId(groupId);
        entity.setToId(toId);

        try {
            return imFriendShipGroupMemberMapper.insert(entity);
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req) {

        ResponseVO<ImFriendShipGroupEntity> group = imFriendShipGroupService
                .getGroup(req.getFromId(), req.getGroupName(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        List<String> successId = new ArrayList<>();
        for (String toId : req.getToIds()) {
            ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (singleUserInfo.isOk()) {
                int i = doDelGroupMember(group.getData().getGroupId(), toId);
                if (i == 1) {
                    successId.add(toId);
                }
            }
        }

        Long seq = imFriendShipGroupService.updateSeq(req.getFromId(), req.getGroupName(), req.getAppId());

        DeleteFriendGroupMemberPack deleteFriendGroupMemberPack = new DeleteFriendGroupMemberPack();
        deleteFriendGroupMemberPack.setFromId(req.getFromId());
        deleteFriendGroupMemberPack.setGroupName(req.getGroupName());
        deleteFriendGroupMemberPack.setToIds(successId);
        deleteFriendGroupMemberPack.setSequence(seq);
        messageProducer.sendToUserExceptClient(req.getFromId(), FriendshipEventCommand.FRIEND_GROUP_MEMBER_DELETE,
                deleteFriendGroupMemberPack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse(successId);
    }

    public int doDelGroupMember(Long groupId, String toId) {

        QueryWrapper<ImFriendShipGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("to_id", toId);

        try {
            return imFriendShipGroupMemberMapper.delete(queryWrapper);
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0;
        }
    }

    @Override
    public int clearGroupMember(Long groupId) {

        QueryWrapper<ImFriendShipGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);

        return imFriendShipGroupMemberMapper.delete(query);
    }
}
