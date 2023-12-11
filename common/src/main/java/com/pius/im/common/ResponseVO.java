package com.pius.im.common;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVO<T> {

    private int code;

    private String msg;

    private T data;

    public static <K> ResponseVO<K> successResponse(K data) {
        return new ResponseVO<>(200, "success", data);
    }

    public static <K> ResponseVO<K> successResponse() {
        return new ResponseVO<>(200, "success");
    }

    public static <K> ResponseVO<K> errorResponse() {
        return new ResponseVO<>(500, "系统内部异常");
    }

    public static <K> ResponseVO<K> errorResponse(int code, String msg) {
        return new ResponseVO<>(code, msg);
    }

    public static <K> ResponseVO<K> errorResponse(ApplicationExceptionEnum enums) {
        return new ResponseVO<>(enums.getCode(), enums.getError());
    }

    public boolean isOk() {
        return this.code == 200;
    }

    public ResponseVO(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseVO<T> success() {
        this.code = 200;
        this.msg = "success";
        return this;
    }

    public ResponseVO<T> success(T data) {
        this.code = 200;
        this.msg = "success";
        this.data = data;
        return this;
    }
}