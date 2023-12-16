package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Getter
@AllArgsConstructor
public enum GroupStatusEnum {

    /**
     * 群状态 1正常
     *       2解散
     *       其他待定比如封禁
     */
    NORMAL(1),

    DESTROY(2),
    ;

    private int code;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static GroupStatusEnum getEnum(Integer ordinal) {

        if (ordinal == null) {
            return null;
        }

        for (int i = 0; i < GroupStatusEnum.values().length; i++) {
            if (GroupStatusEnum.values()[i].getCode() == ordinal) {
                return GroupStatusEnum.values()[i];
            }
        }

        return null;
    }

}
