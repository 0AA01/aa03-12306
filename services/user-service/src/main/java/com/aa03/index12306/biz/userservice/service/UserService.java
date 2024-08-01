package com.aa03.index12306.biz.userservice.service;

import com.aa03.index12306.biz.userservice.dao.entity.UserDO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户信息接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户详细信息
     */
    UserQueryRespDTO queryUserByUsername(String username);

    /**
     * 根据用户名查询用户无脱敏信息
     *
     * @param username 用户名
     * @return 用户无脱敏详细信息
     */
    UserQueryActualRespDTO queryActualUserByUsername(String username);
}
