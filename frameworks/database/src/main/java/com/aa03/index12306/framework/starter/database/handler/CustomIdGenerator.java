package com.aa03.index12306.framework.starter.database.handler;

import com.aa03.index12306.framework.starter.distributedid.toolkit.SnowflakeIdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

/**
 * 自定义雪花算法生成器
 */
public class CustomIdGenerator implements IdentifierGenerator {
    @Override
    public Number nextId(Object entity) {
        return SnowflakeIdUtil.nextId();
    }
}
