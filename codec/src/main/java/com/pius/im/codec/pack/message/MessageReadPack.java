package com.pius.im.codec.pack.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Data
public class MessageReadPack {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;

}
