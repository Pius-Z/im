package com.pius.im.service.group.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateGroupInfoReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private String groupName;

    /**
     * 全员禁言  0 不禁言
     *         1 全员禁言
     */
    private Integer mute;

    /**
     * 加入群权限  0 所有人可以加入
     *           1 群成员可以拉人
     *           2 群管理员或群组可以拉人
     */
    private Integer applyJoinType;

    /**
     * 群简介
     */
    private String introduction;

    /**
     * 群公告
     */
    private String notification;

    /**
     * 群头像
     */
    private String photo;

    /**
     * 群成员上限
     */
    private Integer maxMemberCount;

    private String extra;

}
