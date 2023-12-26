package com.pius.im.codec.pack.group;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class RemoveGroupMemberPack {

    private String groupId;

    private String member;

}