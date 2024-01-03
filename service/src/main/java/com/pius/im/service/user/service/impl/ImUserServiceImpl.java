package com.pius.im.service.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pius.im.codec.pack.user.UserModifyPack;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.config.AppConfig;
import com.pius.im.common.constant.Constants;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.enums.UserErrorCode;
import com.pius.im.common.enums.command.UserEventCommand;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.service.group.service.ImGroupService;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.dao.mapper.ImUserDataMapper;
import com.pius.im.service.user.model.req.*;
import com.pius.im.service.user.model.resp.GetUserInfoResp;
import com.pius.im.service.user.model.resp.ImportOrDeleteUserResp;
import com.pius.im.service.user.service.ImUserService;
import com.pius.im.service.utils.CallbackService;
import com.pius.im.service.utils.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
@Slf4j
@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    CallbackService callbackService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImGroupService imGroupService;

    @Override
    public ResponseVO<ImportOrDeleteUserResp> importUser(ImportUserReq req) {
        if (req.getUserData().size() > 100) {
            return ResponseVO.errorResponse(20000, "导入数量超出上限");
        }

        ImportOrDeleteUserResp resp = new ImportOrDeleteUserResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        ImUserDataEntity imUserDataEntity = new ImUserDataEntity();
        imUserDataEntity.setDelFlag(DelFlagEnum.NORMAL.getCode());

        for (ImUserDataEntity data : req.getUserData()) {
            QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.eq("user_id", data.getUserId());
            queryWrapper.eq("del_flag", DelFlagEnum.DELETE.getCode());
            int update = 0;

            try {
                update = imUserDataMapper.update(imUserDataEntity, queryWrapper);
                if (update > 0) {
                    successId.add(data.getUserId());
                } else {
                    data.setAppId(req.getAppId());
                    int insert = imUserDataMapper.insert(data);
                    if (insert == 1) {
                        successId.add(data.getUserId());
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                errorId.add(data.getUserId());
            }
        }

        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<ImportOrDeleteUserResp> deleteUser(DeleteUserReq req) {
        ImUserDataEntity imUserDataEntity = new ImUserDataEntity();
        imUserDataEntity.setDelFlag(DelFlagEnum.DELETE.getCode());

        List<String> errorId = new ArrayList<>();
        List<String> successId = new ArrayList<>();

        for (String userId : req.getUserId()) {
            QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", req.getAppId());
            queryWrapper.eq("user_id", userId);
            queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());
            int update = 0;

            try {
                update = imUserDataMapper.update(imUserDataEntity, queryWrapper);
                if (update > 0) {
                    successId.add(userId);
                } else {
                    errorId.add(userId);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                errorId.add(userId);
            }
        }

        ImportOrDeleteUserResp resp = new ImportOrDeleteUserResp();
        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId, Integer appId) {
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity imUserDataEntity = imUserDataMapper.selectOne(queryWrapper);
        if (imUserDataEntity == null) {
            return ResponseVO.errorResponse(UserErrorCode.USER_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(imUserDataEntity);
    }

    @Override
    public ResponseVO<GetUserInfoResp> getUserInfo(GetUserInfoReq req) {
        GetUserInfoResp getUserInfoResp = new GetUserInfoResp();

        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.in("user_id", req.getUserIds());
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        List<ImUserDataEntity> imUserDataEntities = imUserDataMapper.selectList(queryWrapper);
        List<String> failUsers = new ArrayList<>();

        HashSet<String> set = new HashSet<>();
        for (ImUserDataEntity imUserDataEntity : imUserDataEntities) {
            set.add(imUserDataEntity.getUserId());
        }

        for (String userId : req.getUserIds()) {
            if (!set.contains(userId)) {
                failUsers.add(userId);
            }
        }

        getUserInfoResp.setUserDataItem(imUserDataEntities);
        getUserInfoResp.setFailUser(failUsers);
        return ResponseVO.successResponse(getUserInfoResp);
    }

    @Override
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {
        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("user_id", req.getUserId());
        queryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity user = imUserDataMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        ImUserDataEntity imUserDataEntity = new ImUserDataEntity();
        BeanUtils.copyProperties(req, imUserDataEntity);

        imUserDataEntity.setAppId(null);
        imUserDataEntity.setUserId(null);
        int update = imUserDataMapper.update(imUserDataEntity, queryWrapper);
        if (update == 1) {
            // 用户资料修改后的多端同步
            UserModifyPack pack = new UserModifyPack();
            BeanUtils.copyProperties(req, pack);
            messageProducer.sendToUser(req.getUserId(), req.getClientType(), req.getImei(), UserEventCommand.USER_MODIFY, pack, req.getAppId());

            if (appConfig.isModifyUserAfterCallback()) {
                callbackService.callback(req.getAppId(), Constants.CallbackCommand.ModifyUserAfter,
                        JSONObject.toJSONString(req));
            }
            return ResponseVO.successResponse();
        }

        throw new ApplicationException(UserErrorCode.MODIFY_USER_ERROR);
    }

    @Override
    public ResponseVO login(LoginReq req) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("user_id", req.getUserId());
        query.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        if (imUserDataMapper.selectOne(query) != null) {
            return ResponseVO.successResponse();
        } else {
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }
    }

    @Override
    public ResponseVO<Map<Object, Object>> getUserSequence(GetUserSequenceReq req) {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(req.getAppId() + ":" + Constants.RedisConstants.SeqPrefix + ":" + req.getUserId());
        Long groupSeq = imGroupService.getUserGroupMaxSeq(req.getUserId(), req.getAppId());
        map.put(Constants.SeqConstants.Group, groupSeq);
        return ResponseVO.successResponse(map);
    }

}