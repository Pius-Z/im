package com.pius.im.common.enums.command;

import lombok.AllArgsConstructor;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@AllArgsConstructor
public enum UserEventCommand implements Command {

    USER_MODIFY(4000),

    /**
     * 4001 用户在线状态变化报文
     */
    USER_ONLINE_STATUS_CHANGE(4001),

    /**
     * 4002 用户在线状态变化通知报文
     */
    USER_ONLINE_STATUS_CHANGE_NOTIFY(4002),

    /**
     * 4003 用户在线状态变化通知同步报文
     */
    USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC(4003),

    ;

    private int command;

    @Override
    public int getCommand() {
        return command;
    }

}
