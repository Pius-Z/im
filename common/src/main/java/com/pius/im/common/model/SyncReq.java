package com.pius.im.common.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/3
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SyncReq extends RequestBase {

    /**
     * 客户端最大seq
     */
    private Long lastSequence;

    /**
     * 一次拉取多少
     */
    private Integer maxLimit;

}
