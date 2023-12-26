package com.pius.im.codec.pack.friendship;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class AddFriendPack {

    private String fromId;

    private String remark;

    private String toId;

    private String addSource;

    private String addWording;

    private Long sequence;

}
