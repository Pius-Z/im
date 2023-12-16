package com.pius.im.service.group.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.AddMemberResp;
import com.pius.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
public interface ImGroupMemberService {

    ResponseVO<List<AddMemberResp>> importGroupMember(ImportGroupMemberReq req);

    ResponseVO<List<AddMemberResp>> addMember(AddGroupMemberReq req);

    ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    ResponseVO removeMember(RemoveGroupMemberReq req);

    ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);

    ResponseVO exitGroup(ExitGroupReq req);

    ResponseVO updateGroupMember(UpdateGroupMemberReq req);

    ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId, Integer appId);

    List<String> getGroupMemberId(String groupId, Integer appId);

    ResponseVO<GetRoleInGroupResp> getRoleInGroup(String groupId, String memberId, Integer appId);

    ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    List<GroupMemberDto> getGroupManager(String groupId, Integer appId);

    ResponseVO transferGroupOwner(String owner, String groupId, Integer appId);

    ResponseVO muteGroupMember(MuteGroupMemberReq req);

}
