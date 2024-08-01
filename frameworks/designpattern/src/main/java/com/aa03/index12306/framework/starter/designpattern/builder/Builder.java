package com.aa03.index12306.framework.starter.designpattern.builder;

import java.io.Serializable;

/**
 * Builder 模式抽象接口
 */
public interface Builder<T> extends Serializable {

    /**
     * 构建方法
     */
    T build();
}
