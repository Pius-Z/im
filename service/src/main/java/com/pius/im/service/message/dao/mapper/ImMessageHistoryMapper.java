package com.pius.im.service.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pius.im.service.message.dao.ImMessageHistoryEntity;

import java.util.Collection;

/**
 * @Author: Pius
 * @Date: 2023/12/29
 */
public interface ImMessageHistoryMapper extends BaseMapper<ImMessageHistoryEntity> {

    /**
     * 批量插入（mysql）
     */
    Integer insertBatchSomeColumn(Collection<ImMessageHistoryEntity> entityList);

}
