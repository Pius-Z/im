package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ModifyUserInfoReq extends RequestBase {

    // 用户id
    @NotEmpty(message = "用户id不能为空")
    private String userId;

    // 用户名称
    private String nickName;

    //位置
    private String location;

    //生日
    private String birthDay;

    private String password;

    // 头像
    private String photo;

    // 性别
    private String userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;

    private String extra;

}
