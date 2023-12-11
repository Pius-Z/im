package com.pius.im.common.enums;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Getter
@AllArgsConstructor
public enum UserErrorCode implements ApplicationExceptionEnum {

    IMPORT_SIZE_BEYOND(20000, "导入數量超出上限"),
    USER_IS_NOT_EXIST(20001, "用户不存在"),
    SERVER_GET_USER_ERROR(20002, "服务获取用户失败"),
    MODIFY_USER_ERROR(20003, "更新用户失败"),
    SERVER_NOT_AVAILABLE(71000, "没有可用的服务"),
    ;

    private int code;
    private String error;

}
