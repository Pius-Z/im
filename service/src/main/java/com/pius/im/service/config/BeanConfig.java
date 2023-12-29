package com.pius.im.service.config;

import com.pius.im.common.config.AppConfig;
import com.pius.im.common.enums.ImRouteStrategyEnum;
import com.pius.im.common.enums.ConsistentHashImplEnum;
import com.pius.im.common.route.RouteHandle;
import com.pius.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

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
    public RouteHandle routeHandle() throws Exception {
        Integer imRouteStrategy = appConfig.getImRouteStrategy();
        ImRouteStrategyEnum routeStrategyEnum = ImRouteStrategyEnum.getEnum(imRouteStrategy);
        String routeClazz = routeStrategyEnum.getClazz();
        RouteHandle routeHandle = (RouteHandle) Class.forName(routeClazz).newInstance();

        if (routeStrategyEnum == ImRouteStrategyEnum.HASH) {
            Method setHash = Class.forName(routeClazz).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashImpl = appConfig.getConsistentHashImpl();
            ConsistentHashImplEnum consistentHashImplEnum = ConsistentHashImplEnum.getEnum(consistentHashImpl);
            String hashImplClazz = consistentHashImplEnum.getClazz();
            AbstractConsistentHash consistentHash = (AbstractConsistentHash) Class.forName(hashImplClazz).newInstance();
            setHash.invoke(routeHandle, consistentHash);
        }

        return routeHandle;
    }

    @Bean
    public EasySqlInjector easySqlInjector () {
        return new EasySqlInjector();
    }

}
