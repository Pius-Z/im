package com.pius.im.service.group.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.group.model.req.*;
import com.pius.im.service.group.model.resp.AddMemberResp;
import com.pius.im.service.group.service.ImGroupMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/15
 */
@RestController
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Autowired
    ImGroupMemberService groupMemberService;

    @RequestMapping("/import")
    public ResponseVO<List<AddMemberResp>> importGroupMember(@RequestBody @Validated ImportGroupMemberReq req) {
        return groupMemberService.importGroupMember(req);
    }

    @RequestMapping("/add")
    public ResponseVO<List<AddMemberResp>> addMember(@RequestBody @Validated AddGroupMemberReq req) {
        return groupMemberService.addMember(req);
    }

    @RequestMapping("/remove")
    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req) {
        return groupMemberService.removeMember(req);
    }

    @RequestMapping("/exit")
    public ResponseVO exitGroup(@RequestBody @Validated ExitGroupReq req) {
        return groupMemberService.exitGroup(req);
    }

    @RequestMapping("/update")
    public ResponseVO updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req) {
        return groupMemberService.updateGroupMember(req);
    }

    @RequestMapping("/mute")
    public ResponseVO muteGroupMember(@RequestBody @Validated MuteGroupMemberReq req) {
        return groupMemberService.muteGroupMember(req);
    }

}
