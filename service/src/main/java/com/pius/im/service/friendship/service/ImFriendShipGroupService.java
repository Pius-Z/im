package com.pius.im.service.friendship.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.pius.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.pius.im.service.friendship.model.req.DeleteFriendShipGroupReq;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
public interface ImFriendShipGroupService {

    ResponseVO addGroup(AddFriendShipGroupReq req);

    ResponseVO deleteGroup(DeleteFriendShipGroupReq req);

    ResponseVO<ImFriendShipGroupEntity> getGroup(String fromId, String groupName, Integer appId);

}
