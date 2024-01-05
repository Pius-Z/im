package com.pius.im.codec.pack.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2024/1/5
 */
@Data
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;

}
