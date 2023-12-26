package com.pius.im.common.enums.command;

import lombok.AllArgsConstructor;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@AllArgsConstructor
public enum UserEventCommand implements Command {

    USER_MODIFY(4000),

    ;

    private int command;

    @Override
    public int getCommand() {
        return command;
    }

}
