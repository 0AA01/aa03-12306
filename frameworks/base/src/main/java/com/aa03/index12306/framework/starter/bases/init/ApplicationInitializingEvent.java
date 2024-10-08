package com.aa03.index12306.framework.starter.bases.init;

import org.springframework.context.ApplicationEvent;

/**
 * 应用初始化事件
 *
 * <p> 规约事件，通过此事件可以查看业务系统所有初始化行为
 */
public class ApplicationInitializingEvent extends ApplicationEvent {

    /**
     * 初始化
     */
    public ApplicationInitializingEvent(Object source) {
        super(source);
    }
}
