package com.pius.im.service.user.model;

import com.pius.im.common.model.ClientInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/4
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserStatusChangeNotifyContent extends ClientInfo {

    private String userId;

    /**
     * 服务端状态 1上线 2离线
     */
    private Integer status;

}

