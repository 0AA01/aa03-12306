package com.aa03.index12306.biz.userservice.service.impl;

import com.aa03.index12306.biz.userservice.common.enums.UserChainMarkEnum;
import com.aa03.index12306.biz.userservice.dto.res.UserRegisterReqDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import com.aa03.index12306.biz.userservice.service.UserLoginService;
import com.aa03.index12306.biz.userservice.toolkit.UserReuseUtil;
import com.aa03.index12306.framework.starter.cache.DistributedCache;
import com.aa03.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.aa03.index12306.biz.userservice.common.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;

/**
 * 用户登录接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;

    @Override
    public Boolean hasNotUsername(String username) {
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            // TODO 实现误判用户名可用，但需要注意缓存穿透
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + UserReuseUtil.hashShardingIdx(username), username);
        }
        return true;
    }

}
