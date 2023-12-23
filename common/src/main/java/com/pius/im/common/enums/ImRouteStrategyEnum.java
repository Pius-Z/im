package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Getter
@AllArgsConstructor
public enum ImRouteStrategyEnum {

    /**
     * 1.随机
     */
    RANDOM(1, "com.pius.im.common.route.algorithm.random.RandomHandle"),

    /**
     * 2.轮训
     */
    LOOP(2, "com.pius.im.common.route.algorithm.loop.LoopHandle"),

    /**
     * 3.一致性哈希
     */
    HASH(3, "com.pius.im.common.route.algorithm.consistenthash.ConsistentHashHandle"),

    ;

    private int code;

    private String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     */
    public static ImRouteStrategyEnum getEnum(int ordinal) {
        for (int i = 0; i < ImRouteStrategyEnum.values().length; i++) {
            if (ImRouteStrategyEnum.values()[i].getCode() == ordinal) {
                return ImRouteStrategyEnum.values()[i];
            }
        }
        return null;
    }

}
