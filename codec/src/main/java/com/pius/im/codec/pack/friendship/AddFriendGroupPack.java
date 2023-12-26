package com.pius.im.codec.pack.friendship;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class AddFriendGroupPack {

    public String fromId;

    private String groupName;

    private Long sequence;

}
