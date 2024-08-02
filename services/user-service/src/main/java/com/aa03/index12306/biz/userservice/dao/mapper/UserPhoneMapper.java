package com.aa03.index12306.biz.userservice.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.aa03.index12306.biz.userservice.dao.entity.UserPhoneDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户手机号持久层
 */
public interface UserPhoneMapper extends BaseMapper<UserPhoneDO> {

    /**
     * 注销用户
     *
     * @param userPhoneDO 注销用户入参
     */
    @Update("update " +
            "   t_user_phone " +
            "set " +
            "   deletion_time = #{userPhoneDO.deletionTime}, " +
            "   del_flag = '1' " +
            "where " +
            "   phone = #{userPhoneDO.phone} and del_flag = '0'")
    void deletionUser(@Param("userPhoneDO") UserPhoneDO userPhoneDO);
}
