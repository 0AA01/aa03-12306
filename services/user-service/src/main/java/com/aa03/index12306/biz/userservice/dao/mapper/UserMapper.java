package com.aa03.index12306.biz.userservice.dao.mapper;

import com.aa03.index12306.biz.userservice.dao.entity.UserDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户信息持久层
 */
public interface UserMapper extends BaseMapper<UserDO> {

    @Update("update " +
            "   t_user " +
            "set " +
            "   deletion_time = #{userDO.deletionTime}, " +
            "   del_flag = '1' " +
            "where " +
            "   username = #{userDO.username} and del_flag = '0'")
    void deletionUser(@Param("userDO") UserDO userDO);
}
