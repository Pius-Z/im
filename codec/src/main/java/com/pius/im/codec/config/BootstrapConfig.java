package com.pius.im.codec.config;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Data
public class BootstrapConfig {

    private TcpConfig im;

    @Data
    public static class TcpConfig {

        // tcp 绑定的端口号
        private Integer tcpPort;

        // webSocket 绑定的端口号
        private Integer webSocketPort;

        // 是否启用webSocket
        private boolean enableWebSocket;

        // boss线程 默认=1
        private Integer bossThreadSize;

        // work线程
        private Integer workThreadSize;

    }

}
