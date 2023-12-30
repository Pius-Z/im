package com.pius.im.tcp.feign;

import com.pius.im.common.ResponseVO;
import com.pius.im.common.model.message.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
public interface FeignMessageService {

    @Headers({"Content-Type: application/json", "Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    ResponseVO checkSendMessage(CheckSendMessageReq checkSendMessageReq);

}
