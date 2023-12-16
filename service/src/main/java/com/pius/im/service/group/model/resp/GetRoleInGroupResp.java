package com.pius.im.service.group.model.resp;

import lombok.Data;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Data
public class GetRoleInGroupResp {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;

}
