package com.aa03.index12306.framework.starter.idempotent.core.token;

import com.aa03.index12306.framework.starter.idempotent.core.IdempotentExecuteHandler;

/**
 * Token 实现幂等接口
 */
public interface IdempotentTokenService extends IdempotentExecuteHandler {

    /**
     * 创建幂等验证Token
     */
    String createToken();
}
