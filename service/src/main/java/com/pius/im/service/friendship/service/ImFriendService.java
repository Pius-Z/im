package com.pius.im.service.friendship.service;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipEntity;
import com.pius.im.service.friendship.model.req.*;
import com.pius.im.service.friendship.model.resp.CheckFriendShipResp;
import com.pius.im.service.friendship.model.resp.ImportFriendShipResp;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
public interface ImFriendService {

    ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req);

    ResponseVO addFriend(AddFriendReq req);

    ResponseVO doAddFriend(String fromId, FriendDto dto, Integer appId);

    ResponseVO updateFriend(UpdateFriendReq req);

    ResponseVO deleteFriend(DeleteFriendReq req);

    ResponseVO deleteAllFriend(DeleteAllFriendReq req);

    ResponseVO<ImFriendShipEntity> getRelation(GetRelationReq req);

    ResponseVO<List<ImFriendShipEntity>> getAllFriendShip(GetAllFriendShipReq req);

    ResponseVO<List<CheckFriendShipResp>> checkFriendShip(CheckFriendShipReq req);

    ResponseVO addBlack(AddFriendShipBlackReq req);

    ResponseVO deleteBlack(DeleteBlackReq req);

    ResponseVO checkBlack(CheckFriendShipReq req);


}
