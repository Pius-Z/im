package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Getter
@AllArgsConstructor
public enum ConsistentHashImplEnum {

    /**
     * TreeMap
     */
    TREE(1, "com.pius.im.common.route.algorithm.consistenthash" + ".TreeMapConsistentHash"),

    /**
     * 自定义map
     */
    CUSTOMER(2, "com.pius.im.common.route.algorithm.consistenthash.xxxx"),

    ;

    private int code;

    private String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static ConsistentHashImplEnum getEnum(int ordinal) {
        for (int i = 0; i < ConsistentHashImplEnum.values().length; i++) {
            if (ConsistentHashImplEnum.values()[i].getCode() == ordinal) {
                return ConsistentHashImplEnum.values()[i];
            }
        }
        return null;
    }

}
