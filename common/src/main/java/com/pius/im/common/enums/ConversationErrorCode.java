package com.pius.im.common.enums;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@Getter
@AllArgsConstructor
public enum ConversationErrorCode implements ApplicationExceptionEnum {

    CONVERSATION_UPDATE_PARAM_ERROR(50000,"会话参数不能全为空"),

    ;

    private int code;

    private String error;

}
