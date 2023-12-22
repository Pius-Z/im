package com.pius.im.tcp.register;

import com.pius.im.codec.config.BootstrapConfig;
import com.pius.im.common.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Pius
 * @Date: 2023/12/22
 */
@Slf4j
@AllArgsConstructor
public class RegistryZK implements Runnable {

    private final ZKit zKit;

    private final String ip;

    private final BootstrapConfig.TcpConfig tcpConfig;

    @Override
    public void run() {
        zKit.createRootNode();
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        log.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);

        String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
        zKit.createNode(webPath);
        log.info("Registry zookeeper webPath success, msg=[{}]", tcpPath);
    }
}
