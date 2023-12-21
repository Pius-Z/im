package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Getter
@AllArgsConstructor
public enum ImConnectStatusEnum {

    /**
     * 管道链接状态,1=在线，2=离线。
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(2),

    ;

    private Integer code;

}
