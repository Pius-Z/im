package com.pius.im.service.user.model.resp;

import com.pius.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> userDataItem;

    private List<String> failUser;

}
