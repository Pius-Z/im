package com.pius.im.codec.pack.user;

import com.pius.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}