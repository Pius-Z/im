package com.pius.im.codec.pack.group;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class CreateGroupPack {

    private String groupId;

    private Integer appId;

    private String ownerId;

    private Integer groupType;

    private String groupName;

    private Integer mute;

    private Integer applyJoinType;

    private Integer privateChat;

    private String introduction;

    private String notification;

    private String photo;

    private Integer status;

    private Long sequence;

    private Long createTime;

    private String extra;

}
