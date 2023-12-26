package com.pius.im.codec.pack.group;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class UpdateGroupInfoPack {

    private String groupId;

    private String groupName;

    private Integer mute;

    private Integer joinType;

    private String introduction;

    private String notification;

    private String photo;

    private Integer maxMemberCount;

    private Long sequence;

}
