package com.pius.im.codec.pack.friendship;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private Long sequence;

}
