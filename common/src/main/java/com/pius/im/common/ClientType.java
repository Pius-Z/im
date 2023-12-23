package com.pius.im.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/22
 */
@Getter
@AllArgsConstructor
public enum ClientType {

    WEBAPI(0, "webApi"),

    WEB(1, "web"),

    IOS(2, "ios"),

    ANDROID(3, "android"),

    WINDOWS(4, "windows"),

    MAC(5, "mac"),

    ;

    private int code;

    private String error;

}
