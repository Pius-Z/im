package com.pius.im.service.conversion.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Data
@TableName("im_conversation_set")
public class ImConversationSetEntity {

    /**
     * 会话id type_fromId_toId
     */
    private String conversationId;

    /**
     * 会话类型
     */
    private Integer conversationType;

    private Integer appId;

    private String fromId;

    private String toId;

    private int isMute;

    private int isTop;

    private Long sequence;

    private Long readSequence;

}
