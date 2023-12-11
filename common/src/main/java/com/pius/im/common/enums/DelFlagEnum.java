package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Getter
@AllArgsConstructor
public enum DelFlagEnum {

    /**
     * 0 正常；1 删除。
     */
    NORMAL(0),

    DELETE(1),
    ;

    private int code;

}
