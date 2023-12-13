package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@Getter
@AllArgsConstructor
public enum AllowFriendTypeEnum {

    /**
     * 需要验证
     */
    NEED(2),

    /**
     * 不需要验证
     */
    NOT_NEED(1),
    ;

    private int code;

}
