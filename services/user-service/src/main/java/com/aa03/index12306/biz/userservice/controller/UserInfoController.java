package com.aa03.index12306.biz.userservice.controller;

import com.aa03.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import com.aa03.index12306.biz.userservice.service.UserLoginService;
import com.aa03.index12306.biz.userservice.service.UserService;
import com.aa03.index12306.framework.starter.convention.result.Result;
import com.aa03.index12306.framework.starter.web.Results;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息控制层
 */
@RestController
@RequiredArgsConstructor
public class UserInfoController {

    private final UserService userService;
    private final UserLoginService userLoginService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/user-service/query")
    public Result<UserQueryRespDTO> queryUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.queryUserByUsername(username));
    }

    /**
     * 根据用户名查询用户无脱敏消息
     */
    @GetMapping("/api/user-service/actual/query")
    public Result<UserQueryActualRespDTO> queryActualUserByUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userService.queryActualUserByUsername(username));
    }

    /**
     * 检查用户名是否已存在
     */
    @GetMapping("/api/user-service/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") @NotEmpty String username) {
        return Results.success(userLoginService.hasUsername(username));
    }
}
