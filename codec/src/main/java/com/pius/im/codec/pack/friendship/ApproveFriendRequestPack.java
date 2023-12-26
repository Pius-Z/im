package com.pius.im.codec.pack.friendship;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class ApproveFriendRequestPack {

    private Long id;

    /**
     * 1同意
     * 2拒绝
     */
    private Integer status;

    private Long sequence;

}
