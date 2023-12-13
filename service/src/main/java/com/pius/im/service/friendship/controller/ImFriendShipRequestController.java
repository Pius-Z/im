package com.pius.im.service.friendship.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.pius.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.pius.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.pius.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.pius.im.service.friendship.service.ImFriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendShipRequestController {

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @RequestMapping("/approveFriendRequest")
    public ResponseVO approveFriendRequest(@RequestBody @Validated ApproveFriendRequestReq req) {
        return imFriendShipRequestService.approveFriendRequest(req);
    }

    @RequestMapping("/getFriendRequest")
    public ResponseVO<List<ImFriendShipRequestEntity>> getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req) {
        return imFriendShipRequestService.getFriendRequest(req);
    }

    @RequestMapping("/readFriendShipRequestReq")
    public ResponseVO readFriendShipRequestReq(@RequestBody @Validated ReadFriendShipRequestReq req) {
        return imFriendShipRequestService.readFriendShipRequestReq(req);
    }
}
