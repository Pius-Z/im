package com.pius.im.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
