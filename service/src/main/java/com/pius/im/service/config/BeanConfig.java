package com.pius.im.service.config;

import com.pius.im.common.config.AppConfig;
import com.pius.im.common.route.RouteHandle;
import com.pius.im.common.route.algorithm.loop.LoopHandle;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Configuration
public class BeanConfig {

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle routeHandle() {
        return new LoopHandle();
    }

}
