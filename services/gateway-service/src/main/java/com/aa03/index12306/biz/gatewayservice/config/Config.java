package com.aa03.index12306.biz.gatewayservice.config;

import lombok.Data;

import java.util.List;

/**
 *过滤器配置
 */
@Data
public class Config {

    /**
     * 黑名单前置路径
     */
    List<String>blackPathPre;
}
