package com.pius.im.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
@Getter
@AllArgsConstructor
public enum CommandType {

    USER("4"),

    FRIEND("3"),

    GROUP("2"),

    MESSAGE("1"),

    ;

    private String commandType;

    public static CommandType getCommandType(String ordinal) {
        for (int i = 0; i < CommandType.values().length; i++) {
            if (CommandType.values()[i].getCommandType().equals(ordinal)) {
                return CommandType.values()[i];
            }
        }
        return null;
    }

}
