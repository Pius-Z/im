package com.pius.im.service.group.model.req;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Data
public class GroupMemberDto {

    private String memberId;

    private String alias;

    /**
     * 群成员类型 0 普通成员
     *          1 管理员
     *          2 群主
     *          3 已经移除的成员
     */
    private Integer role;

    private Long speakDate;

    private String joinType;

    private Long joinTime;

}
