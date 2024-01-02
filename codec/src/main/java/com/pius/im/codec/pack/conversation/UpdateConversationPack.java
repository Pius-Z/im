package com.pius.im.codec.pack.conversation;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Data
public class UpdateConversationPack {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private Integer conversationType;

    private Long sequence;

}
