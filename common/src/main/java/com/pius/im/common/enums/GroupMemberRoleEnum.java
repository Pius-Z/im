package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Getter
@AllArgsConstructor
public enum GroupMemberRoleEnum {

    /**
     * 普通成员
     */
    ORDINARY(0),

    /**
     * 管理员
     */
    MANAGER(1),

    /**
     * 群主
     */
    OWNER(2),

    /**
     * 离开
     */
    LEAVE(3),
    ;

    private int code;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static GroupMemberRoleEnum getEnum(Integer ordinal) {

        for (int i = 0; i < GroupMemberRoleEnum.values().length; i++) {
            if (GroupMemberRoleEnum.values()[i].getCode() == ordinal) {
                return GroupMemberRoleEnum.values()[i];
            }
        }

        return null;
    }

}
