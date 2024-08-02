package com.aa03.index12306.biz.userservice.service.handler.filter.user;

import com.aa03.index12306.biz.userservice.dto.res.UserRegisterReqDTO;
import com.aa03.index12306.biz.userservice.service.UserService;
import com.aa03.index12306.framework.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户注册检查证件号是否多次注销
 */
@Component
@RequiredArgsConstructor
public class UserRegisterCheckDeletionChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO>  {

    private final UserService userService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        Integer userDeletionNum = userService.queryUserDeletionNum(requestParam.getIdType(), requestParam.getIdCard());
        if (userDeletionNum >= 5) {
            throw new ClientException("证件号多次注销账号已被加入黑名单");
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
