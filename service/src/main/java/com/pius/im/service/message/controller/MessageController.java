package com.pius.im.service.message.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.message.model.req.SendMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.message.service.P2PMessageService;
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
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    P2PMessageService p2PMessageService;

    @RequestMapping("/send")
    public ResponseVO<SendMessageResp> send(@RequestBody @Validated SendMessageReq req) {
        return p2PMessageService.send(req);
    }

}
