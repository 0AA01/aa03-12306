package com.aa03.index12306.biz.userservice.service.impl;

import com.aa03.index12306.biz.userservice.service.UserLoginService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Service;

/**
 * 用户登录接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Override
    public Boolean hasUsername(String username) {
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {

        }

        return null;
    }
}
