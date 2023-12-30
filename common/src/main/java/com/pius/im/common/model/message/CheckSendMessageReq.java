package com.pius.im.common.model.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Data
public class CheckSendMessageReq {

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;

}
