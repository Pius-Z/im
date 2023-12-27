package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Getter
@AllArgsConstructor
public enum UserForbiddenTypeEnum {

    /**
     * 0 正常；1 禁用。
     */
    NORMAL(0),

    FORBIDDEN(1),

    ;

    private int code;

}
