package com.pius.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {

    private Integer appId;

    private String fromId;

    private String groupId;

    private Long messageKey;

    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;

}
