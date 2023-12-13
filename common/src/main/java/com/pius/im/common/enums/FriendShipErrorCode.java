package com.pius.im.common.enums;

import com.pius.im.common.exception.ApplicationExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Getter
@AllArgsConstructor
public enum FriendShipErrorCode implements ApplicationExceptionEnum {

    IMPORT_SIZE_BEYOND(30000, "导入數量超出上限"),

    ADD_FRIEND_ERROR(30001, "添加好友失败"),

    TO_IS_YOUR_FRIEND(30002, "对方已经是你的好友"),

    TO_IS_NOT_YOUR_FRIEND(30003, "对方不是你的好友"),

    FRIEND_IS_DELETED(30004, "好友已被删除"),

    DELETE_FRIEND_FAILED(30005, "删除好友失败"),

    RELATION_IS_NOT_EXIST(30006, "关系链记录不存在"),

    FRIEND_IS_BLACK(30007, "好友已被拉黑"),

    TARGET_IS_BLACK_YOU(30008, "对方把你拉黑"),

    ADD_BLACK_ERROR(30009, "添加黑名单失败"),

    FRIEND_IS_NOT_YOUR_BLACK(300010, "好友已经不在你的黑名单内"),

    NOT_APPROVE_OTHER_MAN_REQUEST(30011, "无法审批其他人的好友请求"),

    FRIEND_REQUEST_IS_NOT_EXIST(30012, "好友申请不存在"),
    ;

    private int code;

    private String error;

}
