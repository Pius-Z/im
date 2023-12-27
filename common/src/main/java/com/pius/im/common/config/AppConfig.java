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

    private String privateKey;

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

    /**
     * 添加好友之后回调开关
     */
    private boolean addFriendAfterCallback;

    /**
     * 添加好友之前回调开关
     */
    private boolean addFriendBeforeCallback;

    /**
     * 修改好友之后回调开关
     */
    private boolean modifyFriendAfterCallback;

    /**
     * 删除好友之后回调开关
     */
    private boolean deleteFriendAfterCallback;

    /**
     *
     */
    private boolean addFriendShipBlackAfterCallback;

    /**
     * 删除黑名单之后回调开关
     */
    private boolean deleteFriendShipBlackAfterCallback;

    /**
     * 创建群聊之后回调开关
     */
    private boolean createGroupAfterCallback;

    /**
     * 修改群聊之后回调开关
     */
    private boolean modifyGroupAfterCallback;

    /**
     * 解散群聊之后回调开关
     */
    private boolean destroyGroupAfterCallback;

    /**
     * 删除群成员之后回调
     */
    private boolean deleteGroupMemberAfterCallback;

    /**
     * 拉人入群之前回调
     */
    private boolean addGroupMemberBeforeCallback;

    /**
     * 拉人入群之后回调
     */
    private boolean addGroupMemberAfterCallback;

}
