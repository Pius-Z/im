package com.pius.im.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    /** zk连接地址*/
    private String zkAddr;

    /** zk连接超时时间*/
    private Integer zkConnectTimeOut;

    /** netty服务器路由策略 */
    private Integer imRouteStrategy;

    /** 一致性hash的具体实现 */
    private Integer consistentHashImpl;

    private String callbackUrl;

    /**
     * 用户资料变更之后回调开关
     */
    private boolean modifyUserAfterCallback;

}
