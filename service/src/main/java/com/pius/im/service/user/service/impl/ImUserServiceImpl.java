package com.pius.im.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.DelFlagEnum;
import com.pius.im.common.enums.UserErrorCode;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.dao.mapper.ImUserDataMapper;
import com.pius.im.service.user.model.req.*;
import com.pius.im.service.user.model.resp.GetUserInfoResp;
import com.pius.im.service.user.model.resp.ImportOrDeleteUserResp;
import com.pius.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @Author: Pius
 * @Date: 2023/12/8
 */
@Slf4j
@Service
public class ImUserServiceImpl implements ImUserService {

    @Autowired
    ImUserDataMapper imUserDataMapper;

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
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId , Integer appId) {
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

}