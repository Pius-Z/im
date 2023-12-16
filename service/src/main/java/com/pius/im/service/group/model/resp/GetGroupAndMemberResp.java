package com.pius.im.service.group.model.resp;

import com.pius.im.service.group.model.req.GroupMemberDto;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Data
public class GetGroupAndMemberResp {

    private String groupId;

    private Integer appId;

    private String ownerId;

    private Integer groupType;

    private String groupName;

    private Integer mute;

    private Integer applyJoinType;

    private Integer privateChat;

    private String introduction;

    private String notification;

    private String photo;

    private Integer maxMemberCount;

    private Integer status;

    private List<GroupMemberDto> memberList;

}
