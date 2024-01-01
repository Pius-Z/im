package com.pius.im.codec.pack.message;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Data
@AllArgsConstructor
public class ChatMessageAck {

    private String messageId;

    private Long messageSequence;

    public ChatMessageAck(String messageId) {
        this.messageId = messageId;
    }

}
