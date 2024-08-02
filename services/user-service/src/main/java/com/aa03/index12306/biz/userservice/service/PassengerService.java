package com.aa03.index12306.biz.userservice.service;

import com.aa03.index12306.biz.userservice.dto.res.PassengerReqDTO;

/**
 * 乘车人接口层
 */
public interface PassengerService {

    /**
     * 新增乘车人
     *
     * @param requestParam 乘车人信息
     */
    void savePassenger(PassengerReqDTO requestParam);
}
