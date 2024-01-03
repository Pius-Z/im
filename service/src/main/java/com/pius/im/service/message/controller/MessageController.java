package com.pius.im.service.message.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.command.MessageCommand;
import com.pius.im.common.model.SyncReq;
import com.pius.im.common.model.SyncResp;
import com.pius.im.common.model.message.CheckSendMessageReq;
import com.pius.im.common.model.message.OfflineMessageContent;
import com.pius.im.service.message.model.req.SendMessageReq;
import com.pius.im.service.message.model.resp.SendMessageResp;
import com.pius.im.service.message.service.GroupMessageService;
import com.pius.im.service.message.service.MessageSyncService;
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

    @Autowired
    GroupMessageService groupMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    @RequestMapping("/send")
    public ResponseVO<SendMessageResp> send(@RequestBody @Validated SendMessageReq req) {
        return p2PMessageService.send(req);
    }

    @RequestMapping("/checkSend")
    public ResponseVO checkSend(@RequestBody @Validated CheckSendMessageReq req) {
        if (req.getCommand() == MessageCommand.MSG_P2P.getCommand()) {
            return p2PMessageService.imServerPermissionCheck(req.getFromId(), req.getToId(), req.getAppId());
        } else {
            return groupMessageService.imServerPermissionCheck(req.getFromId(), req.getToId(), req.getAppId());
        }
    }

    @RequestMapping("/syncOfflineMessage")
    public ResponseVO<SyncResp<OfflineMessageContent>> syncOfflineMessage(@RequestBody @Validated SyncReq req) {
        return messageSyncService.syncOfflineMessage(req);
    }

}
