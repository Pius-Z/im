package com.pius.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.GroupErrorCode;
import com.pius.im.common.enums.GroupMemberRoleEnum;
import com.pius.im.common.enums.GroupStatusEnum;
import com.pius.im.common.enums.GroupTypeEnum;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.service.group.dao.ImGroupEntity;
import com.pius.im.service.group.dao.mapper.ImGroupMapper;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.GetGroupAndMemberResp;
import com.pius.im.service.group.model.resp.GetJoinedGroupResp;
import com.pius.im.service.group.model.resp.GetRoleInGroupResp;
import com.pius.im.service.group.service.ImGroupMemberService;
import com.pius.im.service.group.service.ImGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Slf4j
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupMapper;

    @Lazy
    @Autowired
    ImGroupMemberService imGroupMemberService;

    @Override
    @Transactional
    public ResponseVO importGroup(ImportGroupReq req) {

        /*
        查询群是否存在
        若请求中包含群groupId，则查询群是否存在
        若不包含群id，则设置群id
        */
        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            queryWrapper.eq("group_id", req.getGroupId());
            queryWrapper.eq("app_id", req.getAppId());
            Long count = imGroupMapper.selectCount(queryWrapper);
            if (count > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        // 若是公开群，则必须有群主
        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            return ResponseVO.errorResponse(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        // 插入群组
        ImGroupEntity group = new ImGroupEntity();
        group.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, group);
        if (req.getCreateTime() == null) {
            group.setCreateTime(System.currentTimeMillis());
        }

        int insert = imGroupMapper.insert(group);
        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperator());
        }

        /*
        查询群是否存在
        若请求中包含群groupId，则查询群是否存在
        若不包含群id，则设置群id
        */
        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            queryWrapper.eq("group_id", req.getGroupId());
            queryWrapper.eq("app_id", req.getAppId());
            Long count = imGroupMapper.selectCount(queryWrapper);
            if (count > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        // 若是公开群，则必须有群主
        if (req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())) {
            return ResponseVO.errorResponse(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        // 插入群组
        ImGroupEntity group = new ImGroupEntity();
        group.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, group);
        group.setCreateTime(System.currentTimeMillis());

        int insert = imGroupMapper.insert(group);
        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        // 插入群主
        GroupMemberDto groupOwner = new GroupMemberDto();
        groupOwner.setMemberId(req.getOwnerId());
        groupOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupOwner.setJoinTime(System.currentTimeMillis());
        ResponseVO addGroupOwner = imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupOwner);
        if (!addGroupOwner.isOk()) {
            return addGroupOwner;
        }

        // 插入群成员
        for (GroupMemberDto groupMember : req.getMember()) {
            imGroupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMember);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {

        boolean isAdmin = false;

        // 查询群是否存在
        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", req.getGroupId());
        queryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity group = imGroupMapper.selectOne(queryWrapper);

        // 群不存在
        if (group == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        // 群已经被删除
        if (group.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_DESTROY);
        }

        // 私有群不能被删除
        if (group.getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTROY);
        }

        // 公开群只能由群主删除
        if (!isAdmin) {
            if (group.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !group.getOwnerId().equals(req.getOperator())) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        ImGroupEntity entity = new ImGroupEntity();
        entity.setStatus(GroupStatusEnum.DESTROY.getCode());

        int update = imGroupMapper.update(entity, queryWrapper);
        if (update != 1) {
            throw new ApplicationException(GroupErrorCode.DESTROY_GROUP_FAILED);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO updateGroupInfo(UpdateGroupInfoReq req) {

        // 查询群是否存在
        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", req.getGroupId());
        queryWrapper.eq("app_id", req.getAppId());

        ImGroupEntity group = imGroupMapper.selectOne(queryWrapper);
        if (group == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        // 群已删除
        if (group.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_DESTROY);
        }

        boolean isAdmin = false;

        // 校验是否有修改资料的权限
        if (!isAdmin) {
            ResponseVO<GetRoleInGroupResp> roleInfo = imGroupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!roleInfo.isOk()) {
                return roleInfo;
            }

            Integer role = roleInfo.getData().getRole();
            boolean isManager = role == GroupMemberRoleEnum.MANAGER.getCode() || role == GroupMemberRoleEnum.OWNER.getCode();

            // 公开群只有群主或管理员可以修改资料
            if (!isManager && group.getGroupType() == GroupTypeEnum.PUBLIC.getCode()) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity entity = new ImGroupEntity();
        BeanUtils.copyProperties(req, entity);
        entity.setUpdateTime(System.currentTimeMillis());

        int row = imGroupMapper.update(entity, queryWrapper);
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_INFO_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<ImGroupEntity> getGroupInfo(String groupId, Integer appId) {

        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("group_id", groupId);

        ImGroupEntity group = imGroupMapper.selectOne(queryWrapper);
        if (group == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(group);
    }

    @Override
    public ResponseVO<GetGroupAndMemberResp> getGroupAndMember(GetGroupAndMemberReq req) {

        // 获取群组信息
        ResponseVO<ImGroupEntity> group = getGroupInfo(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return ResponseVO.errorResponse(group.getCode(), group.getMsg());
        }

        // 获取群成员信息
        ResponseVO<List<GroupMemberDto>> groupMember = imGroupMemberService.getGroupMember(req.getGroupId(), req.getAppId());
        if (!groupMember.isOk()) {
            return ResponseVO.errorResponse(groupMember.getCode(), groupMember.getMsg());
        }

        GetGroupAndMemberResp resp = new GetGroupAndMemberResp();
        BeanUtils.copyProperties(group.getData(), resp);
        resp.setMemberList(groupMember.getData());

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<GetJoinedGroupResp> getJoinedGroup(GetJoinedGroupReq req) {

        // 查询用户加入的群id列表
        ResponseVO<Collection<String>> memberJoinedGroup = imGroupMemberService.getMemberJoinedGroup(req);
        if (!memberJoinedGroup.isOk()) {
            return ResponseVO.errorResponse(memberJoinedGroup.getCode(), memberJoinedGroup.getMsg());
        }

        GetJoinedGroupResp resp = new GetJoinedGroupResp();

        if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
            resp.setTotalCount(0);
            resp.setGroupList(new ArrayList<>());
            return ResponseVO.successResponse(resp);
        }

        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.in("group_id", memberJoinedGroup.getData());

        List<ImGroupEntity> groupList = imGroupMapper.selectList(queryWrapper);

        resp.setGroupList(groupList);
        resp.setTotalCount(groupList.size());

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {

        // 获取转移人权限，若转移人不是群主，则不能转让
        ResponseVO<GetRoleInGroupResp> roleInfo = imGroupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
        if (!roleInfo.isOk()) {
            return roleInfo;
        }

        if (roleInfo.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        // 获取被转移人权限
        ResponseVO<GetRoleInGroupResp> newOwnerRole = imGroupMemberService.getRoleInGroup(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if (!newOwnerRole.isOk()) {
            return newOwnerRole;
        }

        QueryWrapper<ImGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", req.getGroupId());
        queryWrapper.eq("app_id", req.getAppId());

        ImGroupEntity group = imGroupMapper.selectOne(queryWrapper);
        if (group.getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_DESTROY);
        }

        // 更新群组表中的群主信息
        ImGroupEntity entity = new ImGroupEntity();
        entity.setOwnerId(req.getOwnerId());

        UpdateWrapper<ImGroupEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id", req.getAppId());
        updateWrapper.eq("group_id", req.getGroupId());

        int update = imGroupMapper.update(entity, updateWrapper);
        if (update != 1) {
            return ResponseVO.errorResponse(GroupErrorCode.TRANSFER_GROUP_FAILED);
        }

        // 更新群成员表中的权限信息
        ResponseVO transferGroupOwner = imGroupMemberService.transferGroupOwner(req.getOwnerId(), req.getGroupId(), req.getAppId());
        if (!transferGroupOwner.isOk()) {
            return ResponseVO.errorResponse(GroupErrorCode.TRANSFER_GROUP_FAILED);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO muteGroup(MuteGroupReq req) {

        // 获取群信息
        ResponseVO<ImGroupEntity> group = getGroupInfo(req.getGroupId(), req.getAppId());
        if (!group.isOk()) {
            return group;
        }

        // 群已被删除
        if (group.getData().getStatus() == GroupStatusEnum.DESTROY.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_DESTROY);
        }

        // 私有群不能禁言
        if (group.getData().getGroupType() == GroupTypeEnum.PRIVATE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_MUTE);
        }

        boolean isAdmin = false;

        // 校验是否有禁言的权限
        if (!isAdmin) {
            ResponseVO<GetRoleInGroupResp> roleInfo = imGroupMemberService.getRoleInGroup(req.getGroupId(), req.getOperator(), req.getAppId());
            if (!roleInfo.isOk()) {
                return roleInfo;
            }

            Integer role = roleInfo.getData().getRole();
            boolean isManager = role == GroupMemberRoleEnum.MANAGER.getCode() || role == GroupMemberRoleEnum.OWNER.getCode();

            // 公开群只能由管理禁言
            if (!isManager && group.getData().getGroupType() == GroupTypeEnum.PUBLIC.getCode()) {
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity entity = new ImGroupEntity();
        entity.setMute(req.getMute());

        UpdateWrapper<ImGroupEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("group_id", req.getGroupId());
        updateWrapper.eq("app_id", req.getAppId());

        int update = imGroupMapper.update(entity, updateWrapper);
        if (update == 0) {
            throw new ApplicationException(GroupErrorCode.GROUP_MUTE_FAILED);
        }

        return ResponseVO.successResponse();
    }

}