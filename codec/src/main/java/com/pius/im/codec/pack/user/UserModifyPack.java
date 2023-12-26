package com.pius.im.codec.pack.user;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class UserModifyPack {

    private String userId;

    private String nickName;

    private String password;

    private String photo;

    private String userSex;

    private String selfSignature;

    private Integer friendAllowType;

}
