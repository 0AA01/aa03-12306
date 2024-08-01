package com.aa03.index12306.biz.userservice.service;

/**
 * 用户登录接口层
 */
public interface UserLoginService {

    /**
     * 用户名是否存在
     *
     * @param username 用户名
     * @return 用户名是否存在返回结果
     */
    Boolean hasUsername(String username);
}
