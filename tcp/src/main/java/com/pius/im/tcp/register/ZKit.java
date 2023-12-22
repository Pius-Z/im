package com.pius.im.tcp.register;

import com.pius.im.common.constant.Constants;
import lombok.AllArgsConstructor;
import org.I0Itec.zkclient.ZkClient;

/**
 * @Author: Pius
 * @Date: 2023/12/22
 */
@AllArgsConstructor
public class ZKit {

    private ZkClient zkClient;

    // im-coreRoot/tcp/ip:port
    public void createRootNode() {
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if (!exists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        if (!webExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }
    }

    // ip+port
    public void createNode(String path) {
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path);
        }
    }
}
