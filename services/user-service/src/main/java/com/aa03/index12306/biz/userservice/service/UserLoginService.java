package com.aa03.index12306.biz.userservice.service;

import com.aa03.index12306.biz.userservice.dto.res.UserRegisterReqDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;

/**
 * 用户登录接口层
 */
public interface UserLoginService {

    /**
     * 用户名是否不存在
     *
     * @param username 用户名
     * @return 用户名是否存在返回结果
     */
    Boolean hasNotUsername(String username);

    /**
     * 用户注册
     *
     * @param requestParam 用户注册入参
     * @return 用户注册返回结果
     */
    UserRegisterRespDTO register(UserRegisterReqDTO requestParam);
}
