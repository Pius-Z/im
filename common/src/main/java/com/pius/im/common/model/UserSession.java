package com.pius.im.common.model;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Data
public class UserSession {

    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 客户端标识
     */
    private Integer clientType;

    private Integer version;

    /**
     * 连接状态 1=在线 2=离线
     */
    private Integer connectState;

    private Integer brokerId;

    private String brokerHost;

    private String imei;

}
