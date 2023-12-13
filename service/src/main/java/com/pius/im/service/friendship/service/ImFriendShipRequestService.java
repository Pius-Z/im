package com.pius.im.service.friendship.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.pius.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.pius.im.service.friendship.model.req.FriendDto;
import com.pius.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.pius.im.service.friendship.model.req.ReadFriendShipRequestReq;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
public interface ImFriendShipRequestService {

    ResponseVO addFiendShipRequest(String fromId, FriendDto dto, Integer appId);

    ResponseVO approveFriendRequest(ApproveFriendRequestReq req);

    ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req);

    ResponseVO<List<ImFriendShipRequestEntity>> getFriendRequest(GetFriendShipRequestReq req);

}
