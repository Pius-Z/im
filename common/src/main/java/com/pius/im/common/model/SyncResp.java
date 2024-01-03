package com.pius.im.common.model;

import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2024/1/3
 */
@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
