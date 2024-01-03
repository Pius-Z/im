package com.pius.im.service.user.controller;

import com.pius.im.common.ClientType;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.route.RouteHandle;
import com.pius.im.common.route.RouteInfo;
import com.pius.im.common.utils.RouteInfoParseUtil;
import com.pius.im.service.user.model.req.DeleteUserReq;
import com.pius.im.service.user.model.req.GetUserSequenceReq;
import com.pius.im.service.user.model.req.ImportUserReq;
import com.pius.im.service.user.model.req.LoginReq;
import com.pius.im.service.user.model.resp.ImportOrDeleteUserResp;
import com.pius.im.service.user.service.ImUserService;
import com.pius.im.service.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@RestController
@RequestMapping("v1/user")
public class ImUserController {

    @Autowired
    ImUserService imUserService;

    @Autowired
    ZKit zKit;

    @Autowired
    RouteHandle routeHandle;

    @RequestMapping(value = "importUser", method = RequestMethod.POST)
    public ResponseVO<ImportOrDeleteUserResp> importUser(@RequestBody @Validated ImportUserReq req) {
        return imUserService.importUser(req);
    }

    @RequestMapping(value = "deleteUser", method = RequestMethod.POST)
    public ResponseVO<ImportOrDeleteUserResp> deleteUser(@RequestBody @Validated DeleteUserReq req) {
        return imUserService.deleteUser(req);
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ResponseVO login(@RequestBody @Validated LoginReq req) {
        ResponseVO login = imUserService.login(req);
        if (login.isOk()) {
            List<String> allNode;
            if (req.getClientType() == ClientType.WEB.getCode()) {
                allNode = zKit.getAllWebNode();
            } else {
                allNode = zKit.getAllTcpNode();
            }
            String s = routeHandle.routeServer(allNode, req.getUserId());
            RouteInfo parse = RouteInfoParseUtil.parse(s);
            return ResponseVO.successResponse(parse);
        }

        return ResponseVO.errorResponse();
    }

    @RequestMapping("/getUserSequence")
    public ResponseVO<Map<Object, Object>> getUserSequence(@RequestBody @Validated GetUserSequenceReq req) {
        return imUserService.getUserSequence(req);
    }

}
