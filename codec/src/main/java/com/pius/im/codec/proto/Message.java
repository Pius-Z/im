package com.pius.im.codec.proto;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Data
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", messagePack=" + messagePack +
                '}';
    }
}
