package com.pius.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Data
public class ImportFriendShipResp {

    private List<String> successId;

    private List<String> errorId;

}
