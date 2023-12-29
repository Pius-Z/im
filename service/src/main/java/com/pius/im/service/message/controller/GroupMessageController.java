package com.pius.im.service.message.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.message.model.req.SendGroupMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.message.service.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
@RestController
@RequestMapping("v1/message/group")
public class GroupMessageController {

    @Autowired
    GroupMessageService groupMessageService;

    @RequestMapping("/send")
    public ResponseVO<SendMessageResp> sendMessage(@RequestBody @Validated SendGroupMessageReq req) {
        return groupMessageService.send(req);
    }

}
