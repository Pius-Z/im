package com.pius.im.common.exception;

import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
@Getter
public class ApplicationException extends RuntimeException {

    private int code;

    private String error;

    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
        this.error = message;
    }

    public ApplicationException(ApplicationExceptionEnum exceptionEnum) {
        super(exceptionEnum.getError());
        this.code   = exceptionEnum.getCode();
        this.error  = exceptionEnum.getError();
    }

    /**
     *  avoid the expensive and useless stack trace for api exceptions
     *  @see Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
