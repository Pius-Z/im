package com.pius.im.service.group.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.GetGroupAndMemberResp;
import com.pius.im.service.group.model.resp.GetJoinedGroupResp;
import com.pius.im.service.group.service.ImGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Pius
 * @Date: 2023/12/15
 */

@RestController
@RequestMapping("v1/group")
public class ImGroupController {

    @Autowired
    ImGroupService imGroupService;

    @RequestMapping("/import")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req) {
        return imGroupService.importGroup(req);
    }

    @RequestMapping("/create")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req) {
        return imGroupService.createGroup(req);
    }

    @RequestMapping("/getGroupAndMember")
    public ResponseVO<GetGroupAndMemberResp> getGroupAndMember(@RequestBody @Validated GetGroupAndMemberReq req) {
        return imGroupService.getGroupAndMember(req);
    }

    @RequestMapping("/update")
    public ResponseVO updateGroupInfo(@RequestBody @Validated UpdateGroupInfoReq req) {
        return imGroupService.updateGroupInfo(req);
    }

    @RequestMapping("/getJoinedGroup")
    public ResponseVO<GetJoinedGroupResp> getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req) {
        return imGroupService.getJoinedGroup(req);
    }

    @RequestMapping("/destroy")
    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req) {
        return imGroupService.destroyGroup(req);
    }

    @RequestMapping("/transfer")
    public ResponseVO transferGroup(@RequestBody @Validated TransferGroupReq req) {
        return imGroupService.transferGroup(req);
    }

    @RequestMapping("/mute")
    public ResponseVO mute(@RequestBody @Validated MuteGroupReq req) {
        return imGroupService.muteGroup(req);
    }

}

