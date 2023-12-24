package com.pius.im.service.group.model.callback;

import com.pius.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/24
 */
@Data
public class AddMemberAfterCallbackDto {

    private String groupId;

    private Integer groupType;

    private String operator;

    private List<AddMemberResp> memberId;

}
