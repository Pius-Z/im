package com.pius.im.common.model.message;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/30
 */
@Data
public class ImMessageBody {

    private Integer appId;

    private Long messageKey;

    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
