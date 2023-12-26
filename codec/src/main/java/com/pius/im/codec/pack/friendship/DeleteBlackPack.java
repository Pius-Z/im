package com.pius.im.codec.pack.friendship;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/26
 */
@Data
public class DeleteBlackPack {

    private String fromId;

    private String toId;

    private Long sequence;

}
