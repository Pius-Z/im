package com.pius.im.service.group.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CreateGroupReq extends RequestBase {

    private String groupId;

    /**
     * 群主id
     * 创建群组时会被赋值为 operator, 请求中的 ownerId 近似无效可以忽略
     * 此处存在的目的
     * 1 便于对象属性拷贝
     * 2 app管理员创建群时可以指定群主
     */
    private String ownerId;

    /**
     * 群类型 1私有群（类似微信）
     *       2公开群(类似qq）
     */
    @NotNull(message = "群类型不能为空")
    private Integer groupType;

    private String groupName;

    /**
     * 全员禁言  0 不禁言
     *         1 全员禁言
     */
    private Integer mute;

    /**
     * 加入群权限 0 所有人可以加入
     *          1 群成员可以拉人
     *          2 群管理员或群主可以拉人
     */
    private Integer applyJoinType;

    private String introduction;

    private String notification;

    private String photo;

    private Integer MaxMemberCount;

    private List<GroupMemberDto> member;

    private String extra;

}
