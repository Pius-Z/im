package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@Getter
@AllArgsConstructor
public enum ApproveFriendRequestStatusEnum {

    /**
     * 1 同意；2 拒绝。
     */
    AGREE(1),

    REJECT(2),
    ;

    private int code;

}
