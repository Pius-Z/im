package com.pius.im.service.user.model.req;

import com.pius.im.common.model.RequestBase;
import com.pius.im.service.user.dao.ImUserDataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;

}
