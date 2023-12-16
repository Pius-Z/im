package com.pius.im.common.enums;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Getter
@AllArgsConstructor
public enum GroupErrorCode implements ApplicationExceptionEnum {

    GROUP_IS_NOT_EXIST(40000, "群不存在"),

    GROUP_IS_EXIST(40001, "群已存在"),

    GROUP_IS_HAVE_OWNER(40002, "群已存在群主"),

    USER_IS_JOINED_GROUP(40003, "该用户已经进入该群"),

    USER_JOIN_GROUP_ERROR(40004, "群成员添加失败"),

    GROUP_MEMBER_IS_BEYOND(40005, "群成员已达到上限"),

    MEMBER_IS_NOT_JOINED_GROUP(40006, "该用户不在群内"),

    THIS_OPERATE_NEED_MANAGER_ROLE(40007, "该操作只允许群主/管理员操作"),

    THIS_OPERATE_NEED_APP_MANAGER_ROLE(40008, "该操作只允许APP管理员操作"),

    THIS_OPERATE_NEED_OWNER_ROLE(40009, "该操作只允许群主操作"),

    GROUP_OWNER_IS_NOT_REMOVE(40010, "群主无法移除"),

    UPDATE_GROUP_INFO_ERROR(40011, "更新群信息失败"),

    THIS_GROUP_IS_MUTE(40012, "该群禁止发言"),

    IMPORT_GROUP_ERROR(40013, "导入群组失败"),

    THIS_OPERATE_NEED_ONESELF(40014, "该操作只允许自己操作"),

    PRIVATE_GROUP_CAN_NOT_DESTROY(40015, "私有群不允许解散"),

    PUBLIC_GROUP_MUST_HAVE_OWNER(40016, "公开群必须指定群主"),

    GROUP_MEMBER_IS_SPEAK(40017, "群成员被禁言"),

    GROUP_IS_DESTROY(40018, "群组已解散"),

    DESTROY_GROUP_FAILED(40019, "群组解散失败"),

    GET_GROUP_MEMBER_FAILED(40020, "获取群成员失败"),

    GROUP_MUTE_FAILED(40021, "禁言群失败"),

    PRIVATE_GROUP_CAN_NOT_MUTE(40022, "私有群不允许禁言"),

    TRANSFER_GROUP_FAILED(40023, "转让群失败"),

    REMOVE_MEMBER_FAILED(40024, "删除群成员失败"),

    EXIT_GROUP_FAILED(40025, "退出群聊失败"),

    PUBLIC_GROUP_OWNER_CAN_NOT_EXIT(40026, "公开群的群主不可退群"),

    GROUP_MEMBER_UPDATE_FAILED(40027, "群成员更新失败"),

    PRIVATE_GROUP_CAN_NOT_ADD_MANAGER(40028, "私有群不能设置管理员"),

    GROUP_MEMBER_MUTE_ERROR(40029, "禁言群成员失败"),

    CAN_NOT_UPDATE_ROLE_TO_OWNER(40030, "不能将权限修改为群主"),

    ;

    private int code;

    private String error;

}
