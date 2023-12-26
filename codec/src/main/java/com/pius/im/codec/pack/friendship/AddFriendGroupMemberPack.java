package com.pius.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class AddFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    private Long sequence;

}
