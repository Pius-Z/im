package com.pius.im.service.group.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.group.dao.ImGroupEntity;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.GetGroupAndMemberResp;
import com.pius.im.service.group.model.resp.GetJoinedGroupResp;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
public interface ImGroupService {

    ResponseVO importGroup(ImportGroupReq req);

    ResponseVO createGroup(CreateGroupReq req);

    ResponseVO destroyGroup(DestroyGroupReq req);

    ResponseVO updateGroupInfo(UpdateGroupInfoReq req);

    ResponseVO<ImGroupEntity> getGroupInfo(String groupId, Integer appId);

    ResponseVO<GetGroupAndMemberResp> getGroupAndMember(GetGroupAndMemberReq req);

    ResponseVO<GetJoinedGroupResp> getJoinedGroup(GetJoinedGroupReq req);

    ResponseVO transferGroup(TransferGroupReq req);

    ResponseVO muteGroup(MuteGroupReq req);

}
