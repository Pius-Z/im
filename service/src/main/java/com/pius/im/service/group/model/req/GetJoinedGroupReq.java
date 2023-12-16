package com.pius.im.service.group.model.req;

import com.pius.im.common.model.RequestBase;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GetJoinedGroupReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String memberId;

    private List<Integer> groupType;

    /**
     * 单次拉取的群组数量，如果不填代表所有群组
     */
    private Integer limit;

    /**
     * 第几页
     */
    private Integer offset;

}
