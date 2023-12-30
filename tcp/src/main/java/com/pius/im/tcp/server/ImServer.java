package com.pius.im.tcp.server;

import com.pius.im.codec.MessageDecoder;
import com.pius.im.codec.MessageEncoder;
import com.pius.im.codec.config.BootstrapConfig;
import com.pius.im.tcp.handler.HeartBeatHandler;
import com.pius.im.tcp.handler.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Slf4j
public class ImServer {

    BootstrapConfig.TcpConfig config;
    EventLoopGroup mainGroup;
    EventLoopGroup subGroup;
    ServerBootstrap server;

    public ImServer(BootstrapConfig.TcpConfig config) {
        this.config = config;
        mainGroup = new NioEventLoopGroup(1);
        subGroup = new NioEventLoopGroup(10);
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new MessageDecoder());
                        ch.pipeline().addLast(new MessageEncoder());
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 3));
                        ch.pipeline().addLast(new HeartBeatHandler(config.getHeartBeatTime()));
                        ch.pipeline().addLast(new NettyServerHandler(config.getBrokerId(), config.getLogicUrl()));
                    }
                });
    }

    public void start() {
        this.server.bind(this.config.getTcpPort());
        log.info("tcp start");
    }
}
