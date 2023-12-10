package com.pius.im.common;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.Data;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
@Getter
public enum BaseErrorCode implements ApplicationExceptionEnum {

    SUCCESS(200,"success"),
    SYSTEM_ERROR(90000,"服务器内部错误,请联系管理员"),
    PARAMETER_ERROR(90001,"参数校验错误"),
    ;

    private int code;

    private String error;

    BaseErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
}
