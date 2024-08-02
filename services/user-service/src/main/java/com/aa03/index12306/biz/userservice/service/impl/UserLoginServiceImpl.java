package com.aa03.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aa03.index12306.biz.userservice.common.enums.UserChainMarkEnum;
import com.aa03.index12306.biz.userservice.dao.entity.UserDO;
import com.aa03.index12306.biz.userservice.dao.entity.UserMailDO;
import com.aa03.index12306.biz.userservice.dao.entity.UserPhoneDO;
import com.aa03.index12306.biz.userservice.dao.entity.UserReuseDO;
import com.aa03.index12306.biz.userservice.dao.mapper.UserMailMapper;
import com.aa03.index12306.biz.userservice.dao.mapper.UserMapper;
import com.aa03.index12306.biz.userservice.dao.mapper.UserPhoneMapper;
import com.aa03.index12306.biz.userservice.dao.mapper.UserReuseMapper;
import com.aa03.index12306.biz.userservice.dto.res.UserRegisterReqDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import com.aa03.index12306.biz.userservice.service.UserLoginService;
import com.aa03.index12306.framework.starter.cache.DistributedCache;
import com.aa03.index12306.framework.starter.common.toolkit.BeanUtil;
import com.aa03.index12306.framework.starter.convention.exception.ServiceException;
import com.aa03.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.aa03.index12306.biz.userservice.common.constant.RedisKeyConstant.LOCK_USER_REGISTER;
import static com.aa03.index12306.biz.userservice.common.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;
import static com.aa03.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.*;
import static com.aa03.index12306.biz.userservice.toolkit.UserReuseUtil.hashShardingIdx;

/**
 * 用户登录接口实现层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserMapper userMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMailMapper userMailMapper;
    private final UserReuseMapper userReuseMapper;
    private final RedissonClient redissonClient;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;

    @Override
    public Boolean hasNotUsername(String username) {
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            // TODO 实现误判用户名可用，但需要注意缓存穿透
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        // 责任链模式验证注册用户请求参数是否合规
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + requestParam.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            throw new ServiceException(HAS_USERNAME_NOTNULL);
        }
        try {
            try {
                // 注册用户信息
                int inserted = userMapper.insert(BeanUtil.convert(requestParam, UserDO.class));
                if (inserted < 1) {
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException ex) {
                log.error("用户名 [{}] 重复注册", requestParam.getUsername());
                throw new ServiceException(HAS_USERNAME_NOTNULL);
            }
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(requestParam.getPhone())
                    .username(requestParam.getUsername())
                    .build();
            try {
                // 注册用户手机号信息
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException ex) {
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            if (StrUtil.isNotBlank(requestParam.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(requestParam.getMail())
                        .username(requestParam.getUsername())
                        .build();
                try {
                    // 注册用户邮箱信息
                    userMailMapper.insert(userMailDO);
                } catch (DuplicateKeyException dke) {
                    log.error("用户 [{}] 注册邮箱 [{}] 重复", requestParam.getUsername(), requestParam.getMail());
                    throw new ServiceException(MAIL_REGISTERED);
                }
            }
            String username = requestParam.getUsername();
            userReuseMapper.delete(Wrappers.update(new UserReuseDO(username)));
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(requestParam, UserRegisterRespDTO.class);
    }
}
