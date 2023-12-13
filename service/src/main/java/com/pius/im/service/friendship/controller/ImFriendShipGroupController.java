package com.pius.im.service.friendship.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.pius.im.service.friendship.model.req.*;
import com.pius.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.pius.im.service.friendship.service.ImFriendShipGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@RestController
@RequestMapping("v1/friendship/group")
public class ImFriendShipGroupController {

    @Autowired
    ImFriendShipGroupService imFriendShipGroupService;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated AddFriendShipGroupReq req) {
        return imFriendShipGroupService.addGroup(req);
    }

    @RequestMapping("/del")
    public ResponseVO del(@RequestBody @Validated DeleteFriendShipGroupReq req) {
        return imFriendShipGroupService.deleteGroup(req);
    }

    @RequestMapping("/get")
    public ResponseVO<ImFriendShipGroupEntity> get(String fromId, String groupName, Integer appId) {
        return imFriendShipGroupService.getGroup(fromId, groupName, appId);
    }

    @RequestMapping("/member/add")
    public ResponseVO memberAdd(@RequestBody @Validated AddFriendShipGroupMemberReq req) {
        return imFriendShipGroupMemberService.addGroupMember(req);
    }

    @RequestMapping("/member/del")
    public ResponseVO memberDel(@RequestBody @Validated DeleteFriendShipGroupMemberReq req) {
        return imFriendShipGroupMemberService.delGroupMember(req);
    }

    @RequestMapping("/member/clear/{groupId}")
    public ResponseVO memberClear(@PathVariable("groupId") Long groupId) {
        imFriendShipGroupMemberService.clearGroupMember(groupId);
        return ResponseVO.successResponse();
    }

}
