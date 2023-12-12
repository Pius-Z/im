package com.pius.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.pius.im.common.ResponseVO;
import com.pius.im.common.enums.CheckFriendShipTypeEnum;
import com.pius.im.common.enums.FriendShipErrorCode;
import com.pius.im.common.enums.FriendShipStatusEnum;
import com.pius.im.common.exception.ApplicationException;
import com.pius.im.service.friendship.dao.ImFriendShipEntity;
import com.pius.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.pius.im.service.friendship.model.req.*;
import com.pius.im.service.friendship.model.resp.CheckFriendShipResp;
import com.pius.im.service.friendship.model.resp.ImportFriendShipResp;
import com.pius.im.service.friendship.service.ImFriendService;
import com.pius.im.service.user.dao.ImUserDataEntity;
import com.pius.im.service.user.service.ImUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Pius
 * @Date: 2023/12/11
 */
@Slf4j
@Service
public class ImFriendServiceImpl implements ImFriendService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Override
    public ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }

        ImportFriendShipResp resp = new ImportFriendShipResp();
        List<String> successId = new ArrayList<>();
        List<String> errorId = new ArrayList<>();

        for (ImportFriendShipReq.ImportFriendDto dto : req.getFriendItem()) {
            ImFriendShipEntity entity = new ImFriendShipEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            entity.setFromId(req.getFromId());

            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                errorId.add(dto.getToId());
            }
        }

        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    @Override
    @Transactional
    public ResponseVO addFriend(AddFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        return this.doAddFriend(req.getFromId(), req.getToItem(), req.getAppId());
    }

    public ResponseVO doAddFriend(String fromId, FriendDto dto, Integer appId) {

        //A-B
        //Friend表插入A 和 B 两条记录
        //查询是否有记录存在，如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("from_id", fromId);
        queryWrapper.eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryWrapper);

        if (fromItem == null) {
            //添加逻辑
            ImFriendShipEntity entity = new ImFriendShipEntity();
            entity.setAppId(appId);
            entity.setFromId(fromId);
            BeanUtils.copyProperties(dto, entity);
            entity.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            entity.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            entity.setCreateTime(System.currentTimeMillis());

            int insert = imFriendShipMapper.insert(entity);
            if (insert != 1) {
                throw new ApplicationException(FriendShipErrorCode.ADD_FRIEND_ERROR);
            }
        } else {
            //如果存在则判断状态，如果是已添加，则提示已添加，如果是未添加，则修改状态

            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_YOUR_FRIEND);
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();

                if (StringUtils.isNotBlank(dto.getAddSource())) {
                    update.setAddSource(dto.getAddSource());
                }

                if (StringUtils.isNotBlank(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }

                if (StringUtils.isNotBlank(dto.getExtra())) {
                    update.setExtra(dto.getExtra());
                }

                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

                int result = imFriendShipMapper.update(update, queryWrapper);
                if (result != 1) {
                    throw new ApplicationException(FriendShipErrorCode.ADD_FRIEND_ERROR);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO updateFriend(UpdateFriendReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToItem().getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        return this.doUpdate(req.getFromId(), req.getToItem(), req.getAppId());
    }

    public ResponseVO doUpdate(String fromId, FriendDto dto, Integer appId) {

        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getAddSource, dto.getAddSource())
                .set(ImFriendShipEntity::getExtra, dto.getExtra())
                .set(ImFriendShipEntity::getRemark, dto.getRemark())
                .eq(ImFriendShipEntity::getAppId, appId)
                .eq(ImFriendShipEntity::getFromId, fromId)
                .eq(ImFriendShipEntity::getToId, dto.getToId());

        int update = imFriendShipMapper.update(null, updateWrapper);
        if (update == 0) {
            throw new ApplicationException(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryWrapper);

        if (fromItem == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.TO_IS_NOT_YOUR_FRIEND);
        } else {
            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()) {
                ImFriendShipEntity entity = new ImFriendShipEntity();
                entity.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
                int update = imFriendShipMapper.update(entity, queryWrapper);
                if (update == 0) {
                    throw new ApplicationException(FriendShipErrorCode.DELETE_FRIEND_FAILED);
                }

            } else {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteAllFriend(DeleteAllFriendReq req) {
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("status", FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());

        ImFriendShipEntity entity = new ImFriendShipEntity();
        entity.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETE.getCode());
        imFriendShipMapper.update(entity, queryWrapper);

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO<ImFriendShipEntity> getRelation(GetRelationReq req) {

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity entity = imFriendShipMapper.selectOne(query);
        if (entity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.RELATION_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(entity);
    }

    @Override
    public ResponseVO<List<ImFriendShipEntity>> getAllFriendShip(GetAllFriendShipReq req) {

        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("from_id", req.getFromId());

        return ResponseVO.successResponse(imFriendShipMapper.selectList(queryWrapper));
    }

    @Override
    public ResponseVO<List<CheckFriendShipResp>> checkFriendShip(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp;

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShip(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));

        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {

        ResponseVO<ImUserDataEntity> fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (!fromInfo.isOk()) {
            return fromInfo;
        }

        ResponseVO<ImUserDataEntity> toInfo = imUserService.getSingleUserInfo(req.getToId(), req.getAppId());
        if (!toInfo.isOk()) {
            return toInfo;
        }

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("from_id", req.getFromId());
        query.eq("to_id", req.getToId());

        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(query);

        if (fromItem == null) {
            //走添加逻辑。

            ImFriendShipEntity entity = new ImFriendShipEntity();
            entity.setFromId(req.getFromId());
            entity.setToId(req.getToId());
            entity.setAppId(req.getAppId());
            entity.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
            entity.setCreateTime(System.currentTimeMillis());
            int insert = imFriendShipMapper.insert(entity);
            if (insert != 1) {
                throw new ApplicationException(FriendShipErrorCode.ADD_BLACK_ERROR);
            }

        } else {
            //如果存在则判断状态，如果是拉黑，则提示已拉黑，如果是未拉黑，则修改状态
            if (fromItem.getBlack() != null && fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode()) {
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_BLACK);
            } else {
                ImFriendShipEntity entity = new ImFriendShipEntity();
                entity.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getCode());
                int result = imFriendShipMapper.update(entity, query);
                if (result != 1) {
                    throw new ApplicationException(FriendShipErrorCode.ADD_BLACK_ERROR);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {
        QueryWrapper<ImFriendShipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_id", req.getFromId());
        queryWrapper.eq("app_id", req.getAppId());
        queryWrapper.eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryWrapper);

        if (fromItem.getBlack() == FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_NOT_YOUR_BLACK);
        }

        ImFriendShipEntity entity = new ImFriendShipEntity();
        entity.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
        int update = imFriendShipMapper.update(entity, queryWrapper);
        if (update == 1) {
            return ResponseVO.successResponse();
        }
        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO checkBlack(CheckFriendShipReq req) {

        Map<String, Integer> toIdMap = req.getToIds().stream().collect(Collectors
                .toMap(Function.identity(), s -> 0));

        List<CheckFriendShipResp> resp;

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            resp = imFriendShipMapper.checkFriendShipBlack(req);
        } else {
            resp = imFriendShipMapper.checkFriendShipBlackBoth(req);
        }

        Map<String, Integer> collect = resp.stream()
                .collect(Collectors.toMap(CheckFriendShipResp::getToId, CheckFriendShipResp::getStatus));

        for (String toId : toIdMap.keySet()) {
            if (!collect.containsKey(toId)) {
                CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                checkFriendShipResp.setToId(toId);
                checkFriendShipResp.setFromId(req.getFromId());
                checkFriendShipResp.setStatus(toIdMap.get(toId));
                resp.add(checkFriendShipResp);
            }
        }

        return ResponseVO.successResponse(resp);
    }
}
