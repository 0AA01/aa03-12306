package com.aa03.index12306.biz.userservice.dao.mapper;

import com.aa03.index12306.biz.userservice.dao.entity.UserMailDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户邮箱表持久层
 */
public interface UserMailMapper extends BaseMapper<UserMailDO> {

    /**
     * 注销用户
     *
     * @param userMailDO 注销用户入参
     */
    @Update("update " +
            "   t_user_mail " +
            "set " +
            "   deletion_time = #{userMailDO.deletionTime}, " +
            "   del_flag = '1' " +
            "where " +
            "   mail = #{userMailDO.mail} and del_flag = '0'")
    void deletionUser(@Param("userMailDO") UserMailDO userMailDO);
}
