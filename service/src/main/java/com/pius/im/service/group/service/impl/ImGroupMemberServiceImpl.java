package com.pius.im.service.group.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pius.im.codec.pack.group.AddGroupMemberPack;
import com.pius.im.codec.pack.group.GroupMemberSpeakPack;
import com.pius.im.codec.pack.group.RemoveGroupMemberPack;
import com.pius.im.codec.pack.group.UpdateGroupMemberPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.GroupErrorCode;
import com.pius.im.common.enums.GroupMemberRoleEnum;
import com.pius.im.common.enums.GroupStatusEnum;
import com.pius.im.common.enums.GroupTypeEnum;
import com.pius.im.common.enums.command.GroupEventCommand;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.common.model.ClientInfo;
import com.pius.im.service.group.dao.ImGroupEntity;
import com.pius.im.service.group.dao.ImGroupMemberEntity;
import com.pius.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.pius.im.service.group.model.callback.AddMemberAfterCallbackDto;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.AddMemberResp;
import com.pius.im.service.group.model.resp.GetRoleInGroupResp;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.group.service.ImGroupService;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.service.ImUserService;
import com.pius.im.service.utils.CallbackService;
import com.pius.im.service.utils.GroupMessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Service
@Slf4j
public class ImGroupMemberServiceImpl implements ImGroupMemberService {

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImGroupService imGroupService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Override
    @Transactional
    public ResponseVO<List<AddMemberResp>> importGroupMember(ImportGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        ResponseVO<ImGroupEntity> groupInfo = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!groupInfo.isOk()) {
            return ResponseVO.errorResponse(groupInfo.getCode(), groupInfo.getMsg());
        }

        for (GroupMemberDto memberId : req.getMembers()) {
            ResponseVO addGroupMember = addGroupMember(req.getGroupId(), req.getAppId(), memberId);

            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            addMemberResp.setResultMessage(addGroupMember.getMsg());
            if (addGroupMember.isOk()) {
                addMemberResp.setResult(0);
            } else if (addGroupMember.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                addMemberResp.setResult(2);
            } else {
                addMemberResp.setResult(1);
            }

            resp.add(addMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO<List<AddMemberResp>> addMember(AddGroupMemberReq req) {

        boolean isAdmin = false;

        ResponseVO<ImGroupEntity> groupInfo = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!groupInfo.isOk()) {
            return ResponseVO.errorResponse(groupInfo.getCode(), groupInfo.getMsg());
        }

        // 调用回调函数, 接收可以添加的成员名单
        List<GroupMemberDto> memberDtos = req.getMembers();
        if (appConfig.isAddGroupMemberBeforeCallback()) {
            ResponseVO responseVO = callbackService.beforeCallback(req.getAppId(), Constants.CallbackCommand.GroupMemberAddBefore
                    , JSONObject.toJSONString(req));
            if (!responseVO.isOk()) {
                return ResponseVO.errorResponse(responseVO.getCode(), responseVO.getMsg());
            }

            try {
                memberDtos = JSONArray.parseArray(JSONObject.toJSONString(responseVO.getData()), GroupMemberDto.class);
            } catch (Exception e) {
                log.error("GroupMemberAddBefore 回调失败：{}", req.getAppId());
            }
        }

        // 获取操作人的权限
        ResponseVO<GetRoleInGroupResp> roleInfo = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInfo.isOk()) {
            return ResponseVO.errorResponse(roleInfo.getCode(), roleInfo.getMsg());
        }

        Integer role = roleInfo.getData().getRole();

        boolean isOwner = role == GroupMemberRoleEnum.OWNER.getCode();
        boolean isManager = role == GroupMemberRoleEnum.MANAGER.getCode();

        /*
        私有群（private）	类似普通微信群，创建后仅支持已在群内的好友邀请加群，且无需被邀请方同意或群主审批
        公开群（Public）	类似 QQ 群，创建后群主可以指定群管理员，需要群主或管理员审批通过才能入群
        群类型 1私有群（类似微信） 2公开群(类似qq）
         */
        if (!isAdmin && groupInfo.getData().getGroupType() == GroupTypeEnum.PUBLIC.getCode()
                && (!isOwner && !isManager)) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        List<AddMemberResp> resp = new ArrayList<>();

        for (GroupMemberDto memberId : memberDtos) {
            ResponseVO addGroupMember = addGroupMember(req.getGroupId(), req.getAppId(), memberId);

            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            addMemberResp.setResultMessage(addGroupMember.getMsg());
            if (addGroupMember.isOk()) {
                addMemberResp.setResult(0);
            } else if (addGroupMember.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()) {
                addMemberResp.setResult(2);
            } else {
                addMemberResp.setResult(1);
            }

            resp.add(addMemberResp);
        }

        List<String> members = new ArrayList<>();
        resp.forEach(member -> {
            if (member.getResult() == 0) {
                members.add(member.getMemberId());
            }
        });
        AddGroupMemberPack addGroupMemberPack = new AddGroupMemberPack();
        addGroupMemberPack.setGroupId(req.getGroupId());
        addGroupMemberPack.setMembers(members);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.ADDED_MEMBER, addGroupMemberPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        if (appConfig.isAddGroupMemberAfterCallback()) {
            AddMemberAfterCallbackDto dto = new AddMemberAfterCallbackDto();
            dto.setGroupId(req.getGroupId());
            dto.setGroupType(groupInfo.getData().getGroupType());
            dto.setMemberId(resp);
            dto.setOperator(req.getOperator());
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberAddAfter,
                    JSONObject.toJSONString(dto));
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto groupMemberDto) {

        ResponseVO<ImUserDataEntity> userInfo = imUserService.getSingleUserInfo(groupMemberDto.getMemberId(), appId);
        if (!userInfo.isOk()) {
            return userInfo;
        }

        if (groupMemberDto.getRole() != null && groupMemberDto.getRole() == GroupMemberRoleEnum.OWNER.getCode()) {

            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());

            Long count = imGroupMemberMapper.selectCount(queryOwner);
            if (count > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        QueryWrapper<ImGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("member_id", groupMemberDto.getMemberId());

        ImGroupMemberEntity member = imGroupMemberMapper.selectOne(queryWrapper);
        if (member == null) {
            // 初次加群
            member = new ImGroupMemberEntity();
            BeanUtils.copyProperties(groupMemberDto, member);
            member.setGroupId(groupId);
            member.setAppId(appId);
            member.setJoinTime(System.currentTimeMillis());

            int insert = imGroupMemberMapper.insert(member);
            if (insert != 1) {
                throw new ApplicationException(GroupErrorCode.USER_JOIN_GROUP_ERROR);
            }

            return ResponseVO.successResponse();
        } else if (member.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            // 重新进群
            member = new ImGroupMemberEntity();
            BeanUtils.copyProperties(groupMemberDto, member);
            member.setJoinTime(System.currentTimeMillis());

            int update = imGroupMemberMapper.update(member, queryWrapper);
            if (update != 1) {
                throw new ApplicationException(GroupErrorCode.USER_JOIN_GROUP_ERROR);
            }

            return ResponseVO.successResponse();
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);
    }

    @Override
    @Transactional
    public ResponseVO removeMember(RemoveGroupMemberReq req) {

        boolean isAdmin = false;

        ResponseVO<ImGroupEntity> groupInfo = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!groupInfo.isOk()) {
            return groupInfo;
        }

        ImGroupEntity group = groupInfo.getData();

        if (!isAdmin) {
            // 获取操作人的权限 是管理员or群主or群成员
            ResponseVO<GetRoleInGroupResp> roleInfo = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!roleInfo.isOk()) {
                return roleInfo;
            }

            Integer role = roleInfo.getData().getRole();
            boolean isOwner = role == GroupMemberRoleEnum.OWNER.getCode();
            boolean isManager = role == GroupMemberRoleEnum.MANAGER.getCode();

            // 公开群管理员和群主可踢人
            if (!isOwner && !isManager) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            // 私有群必须是群主才能踢人
            if (group.getGroupType() == GroupTypeEnum.PRIVATE.getCode() && !isOwner) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }

            // 公开群管理员和群主可踢人，但管理员只能踢普通群成员
            if (group.getGroupType() == GroupTypeEnum.PUBLIC.getCode()) {

                // 获取被踢人的权限
                ResponseVO<GetRoleInGroupResp> removeMemberRole = getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
                if (!removeMemberRole.isOk()) {
                    return removeMemberRole;
                }

                // 公开群的群主不能被移除
                if (removeMemberRole.getData().getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                    return ResponseVO.errorResponse(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                }

                // 管理员只能踢普通成员
                if (isManager && removeMemberRole.getData().getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }

            }
        }
        ResponseVO responseVO = removeGroupMember(req.getGroupId(), req.getAppId(), req.getMemberId());
        if (responseVO.isOk()) {
            RemoveGroupMemberPack removeGroupMemberPack = new RemoveGroupMemberPack();
            removeGroupMemberPack.setGroupId(req.getGroupId());
            removeGroupMemberPack.setMember(req.getMemberId());
            groupMessageProducer.producer(req.getMemberId(), GroupEventCommand.DELETED_MEMBER, removeGroupMemberPack,
                    new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

            if (appConfig.isDeleteGroupMemberAfterCallback()) {
                callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberDeleteAfter,
                        JSONObject.toJSONString(req));
            }
        }

        return responseVO;
    }

    @Override
    @Transactional
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId) {

        ResponseVO<ImUserDataEntity> userInfo = imUserService.getSingleUserInfo(memberId, appId);
        if (!userInfo.isOk()) {
            return userInfo;
        }

        // 查询被移除人在群中的状态
        ResponseVO<GetRoleInGroupResp> roleInfo = getRoleInGroup(groupId, memberId, appId);
        if (!roleInfo.isOk()) {
            return roleInfo;
        }

        ImGroupMemberEntity groupMember = new ImGroupMemberEntity();
        groupMember.setRole(GroupMemberRoleEnum.LEAVE.getCode());
        groupMember.setLeaveTime(System.currentTimeMillis());
        groupMember.setGroupMemberId(roleInfo.getData().getGroupMemberId());

        int update = imGroupMemberMapper.updateById(groupMember);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.REMOVE_MEMBER_FAILED);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO exitGroup(ExitGroupReq req) {

        ResponseVO<ImGroupEntity> group = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        ResponseVO<GetRoleInGroupResp> roleInfo = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInfo.isOk()) {
            return roleInfo;
        }

        if (group.getData().getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                roleInfo.getData().getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.PUBLIC_GROUP_OWNER_CAN_NOT_EXIT);
        }

        ImGroupMemberEntity entity = new ImGroupMemberEntity();
        entity.setRole(GroupMemberRoleEnum.LEAVE.getCode());

        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id", req.getGroupId());
        updateWrapper.eq("app_id", req.getAppId());
        updateWrapper.eq("member_id", req.getOperator());

        int update = imGroupMemberMapper.update(entity, updateWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.EXIT_GROUP_FAILED);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req) {

        boolean isAdmin = false;

        ResponseVO<ImGroupEntity> group = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        if (group.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_DESTROY);
        }

        // 是否是自己修改自己的资料
        boolean isMeOperate = req.getOperator().equals(req.getMemberId());

        if (!isAdmin) {
            // 昵称只能自己修改 权限只能群主或管理员修改
            if (!StringUtils.isBlank(req.getAlias()) && !isMeOperate) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_ONESELF);
            }

            // 如果要修改权限相关的则走下面的逻辑
            if (req.getRole() != null) {

                // 私有群不能设置管理员
                if (group.getData().getGroupType() == GroupTypeEnum.PRIVATE.getCode() &&
                        req.getRole() == GroupMemberRoleEnum.MANAGER.getCode()) {
                    return ResponseVO.errorResponse(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_ADD_MANAGER);
                }

                // 获取被操作人权限
                ResponseVO<GetRoleInGroupResp> memberRole = this.getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
                if (!memberRole.isOk()) {
                    return memberRole;
                }

                // 获取操作人权限
                ResponseVO<GetRoleInGroupResp> operatorRole = this.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
                if (!operatorRole.isOk()) {
                    return operatorRole;
                }

                Integer roleInfo = operatorRole.getData().getRole();
                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MANAGER.getCode();

                // 不是管理员或群主不能修改权限
                if (req.getRole() != null && !isOwner && !isManager) {
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                // 管理员只有群主能够设置
                if (req.getRole() != null && req.getRole() == GroupMemberRoleEnum.MANAGER.getCode() && !isOwner) {
                    return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }

            }
        }

        ImGroupMemberEntity entity = new ImGroupMemberEntity();
        entity.setMemberId(req.getMemberId());

        if (StringUtils.isNotBlank(req.getAlias())) {
            entity.setAlias(req.getAlias());
        }

        // 不能直接修改为群主
        if (req.getRole() != null) {
            if (req.getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
                entity.setRole(req.getRole());
            } else {
                return ResponseVO.errorResponse(GroupErrorCode.CAN_NOT_UPDATE_ROLE_TO_OWNER);
            }
        }

        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", req.getAppId());
        updateWrapper.eq("member_id", req.getMemberId());
        updateWrapper.eq("group_id", req.getGroupId());

        int update = imGroupMemberMapper.update(entity, updateWrapper);
        if (update != 1) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_UPDATE_FAILED);
        }

        UpdateGroupMemberPack updateGroupMemberPack = new UpdateGroupMemberPack();
        BeanUtils.copyProperties(req, updateGroupMemberPack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.UPDATED_MEMBER, updateGroupMemberPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId) {
        return ResponseVO.successResponse(imGroupMemberMapper.getGroupMember(appId, groupId));
    }

    @Override
    public List<String> getGroupMemberId(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupMemberId(appId, groupId);
    }

    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroup(String groupId, String memberId, Integer appId) {

        GetRoleInGroupResp resp = new GetRoleInGroupResp();

        QueryWrapper<ImGroupMemberEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("member_id", memberId);

        ImGroupMemberEntity groupMember = imGroupMemberMapper.selectOne(queryWrapper);
        if (groupMember == null || groupMember.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        resp.setSpeakDate(groupMember.getSpeakDate());
        resp.setGroupMemberId(groupMember.getGroupMemberId());
        resp.setMemberId(groupMember.getMemberId());
        resp.setRole(groupMember.getRole());

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req) {

        if (req.getLimit() != null) {
            Page<ImGroupMemberEntity> objectPage = new Page<>(req.getOffset(), req.getLimit());
            QueryWrapper<ImGroupMemberEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.eq("member_id", req.getMemberId());
            if (CollectionUtils.isNotEmpty(req.getGroupType())) {
                queryWrapper.in("group_type", req.getGroupType());
            }

            IPage<ImGroupMemberEntity> imGroupMemberEntityPage = imGroupMemberMapper.selectPage(objectPage, queryWrapper);

            Set<String> groupId = new HashSet<>();
            List<ImGroupMemberEntity> records = imGroupMemberEntityPage.getRecords();
            records.forEach(e -> groupId.add(e.getGroupId()));

            return ResponseVO.successResponse(groupId);
        } else {
            return ResponseVO.successResponse(imGroupMemberMapper.getJoinedGroupId(req.getAppId(), req.getMemberId()));
        }
    }

    @Override
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupManager(groupId, appId);
    }

    @Override
    @Transactional
    public ResponseVO transferGroupOwner(String owner, String groupId, Integer appId) {

        // 更新旧群主
        ImGroupMemberEntity oldOwner = new ImGroupMemberEntity();
        oldOwner.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", appId);
        updateWrapper.eq("group_id", groupId);
        updateWrapper.eq("role", GroupMemberRoleEnum.OWNER.getCode());
        imGroupMemberMapper.update(oldOwner, updateWrapper);

        // 更新新群主
        ImGroupMemberEntity newOwner = new ImGroupMemberEntity();
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        UpdateWrapper<ImGroupMemberEntity> ownerWrapper = new UpdateWrapper<>();
        ownerWrapper.eq("app_id", appId);
        ownerWrapper.eq("group_id", groupId);
        ownerWrapper.eq("member_id", owner);
        imGroupMemberMapper.update(newOwner, ownerWrapper);

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO muteGroupMember(MuteGroupMemberReq req) {

        // 获取分组信息
        ResponseVO<ImGroupEntity> groupInfo = imGroupService.getGroupInfo(req.getGroupId(), req.getAppId());
        if (!groupInfo.isOk()) {
            return groupInfo;
        }

        // 私有群不允许禁言
        if (groupInfo.getData().getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_MUTE);
        }

        // 获取操作人的权限
        ResponseVO<GetRoleInGroupResp> operatorRole = getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!operatorRole.isOk()) {
            return operatorRole;
        }

        // 获取被禁言人的权限
        ResponseVO<GetRoleInGroupResp> memberRole = getRoleInGroup(req.getGroupId(), req.getMemberId(), req.getAppId());
        if (!memberRole.isOk()) {
            return memberRole;
        }

        boolean isAdmin = false;
        boolean isOwner = operatorRole.getData().getRole() == GroupMemberRoleEnum.OWNER.getCode();
        boolean isManager = operatorRole.getData().getRole() == GroupMemberRoleEnum.MANAGER.getCode();

        if (!isAdmin) {

            if (!isOwner && !isManager) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

            // 被操作人是群主只能app管理员操作
            if (memberRole.getData().getRole() == GroupMemberRoleEnum.OWNER.getCode()) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_APP_MANAGER_ROLE);
            }

            // 是管理员并且被操作人不是群成员，无法操作
            if (isManager && memberRole.getData().getRole() != GroupMemberRoleEnum.ORDINARY.getCode()) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        ImGroupMemberEntity entity = new ImGroupMemberEntity();
        entity.setGroupMemberId(memberRole.getData().getGroupMemberId());
        if (req.getSpeakDate() > 0) {
            entity.setSpeakDate(System.currentTimeMillis() + req.getSpeakDate());
        } else {
            entity.setSpeakDate(req.getSpeakDate());
        }

        int update = imGroupMemberMapper.updateById(entity);
        if (update != 1) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_MUTE_ERROR);
        }

        GroupMemberSpeakPack groupMemberSpeakPack = new GroupMemberSpeakPack();
        BeanUtils.copyProperties(req, groupMemberSpeakPack);
        groupMessageProducer.producer(req.getOperator(), GroupEventCommand.SPEAK_GROUP_MEMBER, groupMemberSpeakPack,
                new ClientInfo(req.getAppId(), req.getClientType(), req.getImei()));

        return ResponseVO.successResponse();
    }
}
