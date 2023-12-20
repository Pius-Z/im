package com.pius.im.codec.proto;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/20
 */
@Data
public class MessageHeader {

    // 4字节 消息操作指令 十六进制 一个消息的开始通常以0x开头
    private Integer command;

    // 4字节 版本号
    private Integer version;

    // 4字节 客户端类型
    private Integer clientType;

    // 4字节 appId
    private Integer appId;

    // 4字节 解析类型 0x0:Json, 0x1:ProtoBuf, 0x2:Xml, 默认:0x0
    private Integer messageType = 0x0;

    // 4字节 imei长度
    private Integer imeiLength;

    // 4字节 请求体长度
    private int bodyLength;

    // imei号
    private String imei;
}
