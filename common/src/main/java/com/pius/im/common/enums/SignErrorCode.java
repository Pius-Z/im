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
public enum SignErrorCode implements ApplicationExceptionEnum {

    USER_SIGN_NOT_EXIST(60000, "用户签名不存在"),

    APPID_NOT_EXIST(60001, "appId不存在"),

    OPERATOR_NOT_EXIST(60002, "操作人不存在"),

    USER_SIGN_IS_ERROR(60003, "用户签名不正确"),

    USER_SIGN_OPERATE_NOT_MATE(60005, "用户签名与操作人不匹配"),

    USER_SIGN_IS_EXPIRED(60004, "用户签名已过期"),

    ;

    private int code;

    private String error;

}
