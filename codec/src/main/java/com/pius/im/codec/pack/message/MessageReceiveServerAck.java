package com.pius.im.codec.pack.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/31
 */
@Data
public class MessageReceiveServerAck {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageSequence;

    private Boolean serverSend;

}
