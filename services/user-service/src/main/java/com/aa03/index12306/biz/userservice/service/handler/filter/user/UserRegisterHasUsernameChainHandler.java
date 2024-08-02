package com.aa03.index12306.biz.userservice.service.handler.filter.user;

import com.aa03.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum;
import com.aa03.index12306.biz.userservice.dto.res.UserRegisterReqDTO;
import com.aa03.index12306.biz.userservice.service.UserLoginService;
import com.aa03.index12306.framework.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户注册用户名唯一校验
 */
@Component
@RequiredArgsConstructor
public class UserRegisterHasUsernameChainHandler implements UserRegisterCreateChainFilter<UserRegisterReqDTO> {

    private final UserLoginService userLoginService;

    @Override
    public void handler(UserRegisterReqDTO requestParam) {
        if (!userLoginService.hasNotUsername(requestParam.getUsername())) {
            throw new ClientException(UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
