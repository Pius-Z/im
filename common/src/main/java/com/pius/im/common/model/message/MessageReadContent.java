package com.pius.im.common.model.message;

import com.pius.im.common.model.ClientInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2024/1/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessageReadContent extends ClientInfo {

    private long messageSequence;

    private String fromId;

    private String groupId;

    private String toId;

    private Integer conversationType;

}
