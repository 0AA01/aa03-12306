

package com.aa03.index12306.biz.payservice.dto.base;

import lombok.Getter;
import lombok.Setter;
import com.aa03.index12306.framework.starter.distributedid.toolkit.SnowflakeIdUtil;

/**
 * 抽象退款入参实体

 */
public abstract class AbstractRefundRequest implements RefundRequest {

    /**
     * 交易环境，H5、小程序、网站等
     */
    @Getter
    @Setter
    private Integer tradeType;

    /**
     * 订单号
     */
    @Getter
    @Setter
    private String orderSn;

    /**
     * 支付渠道
     */
    @Getter
    @Setter
    private Integer channel;

    @Override
    public AliRefundRequest getAliRefundRequest() {
        return null;
    }

    @Override
    public String buildMark() {
        return null;
    }
}
