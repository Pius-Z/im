package com.pius.im.service.conversion.controller;

import com.pius.im.common.ResponseVO;
import com.pius.im.service.conversion.model.DeleteConversationReq;
import com.pius.im.service.conversion.model.UpdateConversationReq;
import com.pius.im.service.conversion.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@RestController
@RequestMapping("v1/conversation")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @RequestMapping("/delete")
    public ResponseVO deleteConversation(@RequestBody @Validated DeleteConversationReq deleteConversationReq) {
        return conversationService.deleteConversation(deleteConversationReq);
    }

    @RequestMapping("/update")
    public ResponseVO updateConversation(@RequestBody @Validated UpdateConversationReq updateConversationReq) {
        return conversationService.updateConversation(updateConversationReq);
    }

}
