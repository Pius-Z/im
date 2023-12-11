package com.pius.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/10
 */
@Data
public class ImportOrDeleteUserResp {

    private List<String> successId;

    private List<String> errorId;
}
