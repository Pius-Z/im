package com.pius.im.common.enums.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Getter
@AllArgsConstructor
public enum ConversationEventCommand implements Command {

    /**
     * 删除会话
     */
    CONVERSATION_DELETE(5000),

    /**
     * 更新会话
     */
    CONVERSATION_UPDATE(5001),

    ;

    private int command;

}
