package com.aa03.index12306.biz.userservice.service.impl;

import com.aa03.index12306.biz.userservice.dao.entity.UserDo;
import com.aa03.index12306.biz.userservice.dao.mapper.UserMapper;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import com.aa03.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import com.aa03.index12306.biz.userservice.service.UserService;
import com.aa03.index12306.framework.starter.common.toolkit.BeanUtil;
import com.aa03.index12306.framework.starter.convention.exception.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户信息接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

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
}
