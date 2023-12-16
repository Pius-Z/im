package com.pius.im.service.group.model.resp;

import com.pius.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/14
 */
@Data
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<ImGroupEntity> groupList;

}
