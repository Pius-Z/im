package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/12
 */
@Getter
@AllArgsConstructor
public enum CheckFriendShipTypeEnum {

    /**
     * 1 单方校验；2双方校验。
     */
    SINGLE(1),

    BOTH(2),
    ;

    private int type;

}
