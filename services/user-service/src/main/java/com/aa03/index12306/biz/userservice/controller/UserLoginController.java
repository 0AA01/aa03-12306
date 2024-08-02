package com.aa03.index12306.biz.userservice.controller;

import com.aa03.index12306.biz.userservice.dto.res.UserLoginReqDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import com.aa03.index12306.biz.userservice.service.UserLoginService;
import com.aa03.index12306.framework.starter.convention.result.Result;
import com.aa03.index12306.framework.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户登录控制层
 */
@RestController
@RequiredArgsConstructor
public class UserLoginController {

    private final UserLoginService userLoginService;

    /**
     * 用户登录
     */
    @PostMapping("/api/user-service/v1/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam) {
        return Results.success(userLoginService.login(requestParam));
    }

}
