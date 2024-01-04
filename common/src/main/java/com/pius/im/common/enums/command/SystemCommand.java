package com.pius.im.common.enums.command;

import lombok.AllArgsConstructor;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@AllArgsConstructor
public enum SystemCommand implements Command {

    /**
     * 心跳 9999
     */
    PING(0x270f),

    /**
     * 登录 9000
     */
    LOGIN(0x2328),

    /**
     * 登录ack  9001
     */
    LOGINACK(0x2329),

    /**
     * 登出 9003
     */
    LOGOUT(0x232b),

    /**
     * 下线通知 用于多端互斥  9002
     */
    MULTILOGIN(0x232a),

    ;

    private int command;

    @Override
    public int getCommand() {
        return command;
    }

}
