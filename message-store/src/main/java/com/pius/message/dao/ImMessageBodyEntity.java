package com.pius.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;

    private Long messageKey;

    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
