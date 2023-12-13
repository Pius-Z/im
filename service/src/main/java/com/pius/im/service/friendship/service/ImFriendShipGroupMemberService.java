package com.pius.im.service.friendship.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.pius.im.service.friendship.model.req.DeleteFriendShipGroupMemberReq;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
public interface ImFriendShipGroupMemberService {

    ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    ResponseVO delGroupMember(DeleteFriendShipGroupMemberReq req);

    int clearGroupMember(Long groupId);

}
