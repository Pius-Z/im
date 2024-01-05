package com.pius.im.common.enums;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/27
 */
@Getter
@AllArgsConstructor
public enum MessageErrorCode implements ApplicationExceptionEnum {

    FROM_IS_MUTE(50002, "发送方被禁言"),

    FROM_IS_FORBIDDEN(50003, "发送方被禁用"),

    MESSAGE_BODY_IS_NOT_EXIST(50003, "消息体不存在"),

    MESSAGE_RECALL_TIME_OUT(50004, "消息已超过可撤回时间"),

    MESSAGE_IS_RECALLED(50005, "消息已被撤回"),

    ;

    private int code;

    private String error;

}
