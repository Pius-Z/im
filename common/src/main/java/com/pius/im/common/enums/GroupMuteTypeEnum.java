package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Getter
@AllArgsConstructor
public enum GroupMuteTypeEnum {

    /**
     * 是否全员禁言
     * 0 不禁言
     * 1 全员禁言。
     */
    NOT_MUTE(0),

    MUTE(1),

    ;

    private int code;

}
