package com.pius.im.service.utils;

import com.pius.im.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/23
 */
@Slf4j
@Component
public class ZKit {

    @Autowired
    private ZkClient zkClient;

    /**
     * get all TCP server node from zookeeper
     */
    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        // log.info("Query all tcp node =[{}] success.", children);
        return children;
    }

    /**
     * get all WEB server node from zookeeper
     */
    public List<String> getAllWebNode() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        // log.info("Query all web node =[{}] success.", children);
        return children;
    }

}
