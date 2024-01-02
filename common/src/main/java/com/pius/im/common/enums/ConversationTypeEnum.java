package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Getter
@AllArgsConstructor
public enum ConversationTypeEnum {

    /**
     * 0 单聊
     */
    P2P(0),

    /**
     * 1群聊
     */
    GROUP(1),

    /**
     * 2机器人
     */
    ROBOT(2),

    ;

    private int code;

}
