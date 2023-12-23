package com.pius.im.tcp.receiver;

import com.alibaba.fastjson.JSONObject;
import com.pius.im.codec.proto.MessagePack;
import com.pius.im.common.ClientType;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.DeviceMultiLoginEnum;
import com.pius.im.common.enums.command.SystemCommand;
import com.pius.im.common.model.UserClientDto;
import com.pius.im.tcp.redis.RedisManager;
import com.pius.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/22
 */
@Slf4j
public class UserLoginMessageListener {

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String msg) {
                log.info("收到用户上线通知：" + msg);
                UserClientDto dto = JSONObject.parseObject(msg, UserClientDto.class);
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(dto.getAppId(), dto.getUserId());

                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
                    String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();

                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MULTILOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }

                        if (!(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MULTILOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }

                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        if (dto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        boolean isSameClient = false;
                        if ((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) &&
                                (dto.getClientType() == ClientType.IOS.getCode() || dto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }

                        if ((clientType == ClientType.MAC.getCode() || clientType == ClientType.WINDOWS.getCode()) &&
                                (dto.getClientType() == ClientType.MAC.getCode() || dto.getClientType() == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }

                        if (isSameClient && !(clientType + ":" + imei).equals(dto.getClientType() + ":" + dto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MULTILOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }
                }
            }
        });
    }
}