package com.aa03.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.aa03.index12306.biz.userservice.dao.entity.UserDO;
import com.aa03.index12306.biz.userservice.dao.entity.UserDeletionDO;
import com.aa03.index12306.biz.userservice.dao.entity.UserMailDO;
import com.aa03.index12306.biz.userservice.dao.mapper.UserDeletionMapper;
import com.aa03.index12306.biz.userservice.dao.mapper.UserMailMapper;
import com.aa03.index12306.biz.userservice.dao.mapper.UserMapper;
import com.aa03.index12306.biz.userservice.dto.res.UserUpdateReqDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import com.aa03.index12306.biz.userservice.service.UserService;
import com.aa03.index12306.framework.starter.common.toolkit.BeanUtil;
import com.aa03.index12306.framework.starter.convention.exception.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/**
 * 用户信息接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final UserMailMapper userMailMapper;
    private final UserDeletionMapper userDeletionMapper;

    @Override
    public UserQueryRespDTO queryUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO UserDO = baseMapper.selectOne(queryWrapper);
        if (UserDO == null) {
            throw new ClientException("用户名不存在，请检查用户名是否正确");
        }
        return BeanUtil.convert(UserDO, UserQueryRespDTO.class);
    }

    @Override
    public UserQueryActualRespDTO queryActualUserByUsername(String username) {
        return BeanUtil.convert(queryUserByUsername(username), UserQueryActualRespDTO.class);
    }

    @Override
    public Integer queryUserDeletionNum(Integer idType, String idCard) {
        LambdaQueryWrapper<UserDeletionDO> queryWrapper = Wrappers.lambdaQuery(UserDeletionDO.class)
                .eq(UserDeletionDO::getIdType, idType)
                .eq(UserDeletionDO::getIdCard, idCard);
        // TODO 此处应该先查缓存
        Long deletionCount = userDeletionMapper.selectCount(queryWrapper);
        return Optional.ofNullable(deletionCount).map(Long::intValue).orElse(0);
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        UserQueryRespDTO userQueryRespDTO = queryUserByUsername(requestParam.getUsername());
        UserDO userDO = BeanUtil.convert(requestParam, UserDO.class);
        LambdaQueryWrapper<UserDO> userUpdateWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(userDO, userUpdateWrapper);
        if (StrUtil.isNotBlank(requestParam.getMail()) && !Objects.equals(requestParam.getMail(), userQueryRespDTO.getMail())) {
            LambdaUpdateWrapper<UserMailDO> updateWrapper = Wrappers.lambdaUpdate(UserMailDO.class)
                    .eq(UserMailDO::getMail, userQueryRespDTO.getMail());
            userMailMapper.delete(updateWrapper);
            UserMailDO userMailDO = UserMailDO.builder()
                    .mail(requestParam.getMail())
                    .username(requestParam.getUsername())
                    .build();
            userMailMapper.insert(userMailDO);
        }
    }
}
