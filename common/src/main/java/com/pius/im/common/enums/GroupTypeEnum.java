package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Getter
@AllArgsConstructor
public enum GroupTypeEnum {

    /**
     * 群类型 1私有群（类似微信）
     *       2公开群(类似qq）
     */
    PRIVATE(1),

    PUBLIC(2),
    ;

    private int code;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static GroupTypeEnum getEnum(Integer ordinal) {

        if (ordinal == null) {
            return null;
        }

        for (int i = 0; i < GroupTypeEnum.values().length; i++) {
            if (GroupTypeEnum.values()[i].getCode() == ordinal) {
                return GroupTypeEnum.values()[i];
            }
        }

        return null;
    }

}
