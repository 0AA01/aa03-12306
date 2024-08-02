package com.aa03.index12306.biz.userservice.service;

import com.aa03.index12306.biz.userservice.dao.entity.UserDO;
import com.aa03.index12306.biz.userservice.dto.res.UserUpdateReqDTO;
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

    /**
     * 根据证件类型和证件号查询注销次数
     *
     * @param idType 证件类型
     * @param idCard 证件号
     * @return 注销次数
     */
    Integer queryUserDeletionNum(Integer idType, String idCard);

    /**
     * 根据用户名修改用户信息
     *
     * @param requestParam 修改用户信息请求参数
     */
    void update(UserUpdateReqDTO requestParam);
}
