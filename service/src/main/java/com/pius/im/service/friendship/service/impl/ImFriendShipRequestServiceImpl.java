package com.pius.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.pius.im.codec.pack.friendship.ApproveFriendRequestPack;
import com.pius.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.ApproveFriendRequestStatusEnum;
import com.pius.im.common.enums.FriendShipErrorCode;
import com.pius.im.common.enums.command.FriendshipEventCommand;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.pius.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.pius.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.pius.im.service.friendship.model.req.FriendDto;
import com.pius.im.service.friendship.model.req.GetFriendShipRequestReq;
import com.pius.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.pius.im.service.friendship.service.ImFriendService;
import com.pius.im.service.friendship.service.ImFriendShipRequestService;
import com.pius.im.service.utils.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/13
 */
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {

    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Autowired
    ImFriendService imFriendService;

    @Autowired
    MessageProducer messageProducer;

    @Override
    public ResponseVO addFiendShipRequest(String fromId, FriendDto dto, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", dto.getToId());
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(queryWrapper);

        if (request == null) {
            request = new ImFriendShipRequestEntity();
            request.setAppId(appId);
            request.setFromId(fromId);
            request.setToId(dto.getToId());
            request.setAddSource(dto.getAddSource());
            request.setAddWording(dto.getAddWording());
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setCreateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.insert(request);

        } else {
            // 修改记录内容 和更新时间
            if (StringUtils.isNotBlank(dto.getAddSource())) {
                request.setAddSource(dto.getAddSource());
            }
            if (StringUtils.isNotBlank(dto.getRemark())) {
                request.setRemark(dto.getRemark());
            }
            if (StringUtils.isNotBlank(dto.getAddWording())) {
                request.setAddWording(dto.getAddWording());
            }
            request.setApproveStatus(0);
            request.setReadStatus(0);
            request.setUpdateTime(System.currentTimeMillis());
            imFriendShipRequestMapper.updateById(request);
        }

        // 发送好友申请的tcp给接收方
        messageProducer.sendToUser(dto.getToId(), null, "",
                FriendshipEventCommand.FRIEND_REQUEST, request, appId);

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO approveFriendRequest(ApproveFriendRequestReq req) {

        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectById(req.getId());
        if (request == null) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if (!req.getOperator().equals(request.getToId())) {
            // 只能审批发给自己的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVE_OTHER_MAN_REQUEST);
        }

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setId(req.getId());
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        imFriendShipRequestMapper.updateById(update);

        if (req.getStatus() == ApproveFriendRequestStatusEnum.AGREE.getCode()) {
            // 同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(request.getAddSource());
            dto.setAddWording(request.getAddWording());
            dto.setRemark(request.getRemark());
            dto.setToId(request.getToId());
            ResponseVO responseVO = imFriendService.doAddFriend(null, request.getFromId(), dto, req.getAppId());
            if (!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.TO_IS_YOUR_FRIEND.getCode()) {
                return responseVO;
            }
        }

        ApproveFriendRequestPack approveFriendRequestPack = new ApproveFriendRequestPack();
        approveFriendRequestPack.setId(req.getId());
        messageProducer.sendToUser(request.getToId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_APPROVE, approveFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO readFriendShipRequestReq(ReadFriendShipRequestReq req) {
        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("to_id", req.getFromId());

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        imFriendShipRequestMapper.update(update, queryWrapper);

        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImei(),
                FriendshipEventCommand.FRIEND_REQUEST_READ, readAllFriendRequestPack, req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<List<ImFriendShipRequestEntity>> getFriendRequest(GetFriendShipRequestReq req) {

        QueryWrapper<ImFriendShipRequestEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("to_id", req.getFromId());

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(queryWrapper);

        return ResponseVO.successResponse(requestList);
    }
}
