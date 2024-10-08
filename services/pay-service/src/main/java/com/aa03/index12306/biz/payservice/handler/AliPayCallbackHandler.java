

package com.aa03.index12306.biz.payservice.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.aa03.index12306.biz.payservice.common.enums.PayChannelEnum;
import com.aa03.index12306.biz.payservice.common.enums.TradeStatusEnum;
import com.aa03.index12306.biz.payservice.dto.PayCallbackReqDTO;
import com.aa03.index12306.biz.payservice.dto.base.AliPayCallbackRequest;
import com.aa03.index12306.biz.payservice.dto.base.PayCallbackRequest;
import com.aa03.index12306.biz.payservice.handler.base.AbstractPayCallbackHandler;
import com.aa03.index12306.biz.payservice.service.PayService;
import com.aa03.index12306.framework.starter.designpattern.strategy.AbstractExecuteStrategy;
import org.springframework.stereotype.Service;

/**
 * 阿里支付回调组件

 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class AliPayCallbackHandler extends AbstractPayCallbackHandler implements AbstractExecuteStrategy<PayCallbackRequest, Void> {

    private final PayService payService;

    @Override
    public void callback(PayCallbackRequest payCallbackRequest) {
        AliPayCallbackRequest aliPayCallBackRequest = payCallbackRequest.getAliPayCallBackRequest();
        PayCallbackReqDTO payCallbackRequestParam = PayCallbackReqDTO.builder()
                .status(TradeStatusEnum.queryActualTradeStatusCode(aliPayCallBackRequest.getTradeStatus()))
                .payAmount(aliPayCallBackRequest.getBuyerPayAmount())
                .tradeNo(aliPayCallBackRequest.getTradeNo())
                .gmtPayment(aliPayCallBackRequest.getGmtPayment())
                .orderSn(aliPayCallBackRequest.getOrderRequestId())
                .build();
        payService.callbackPay(payCallbackRequestParam);
    }

    @Override
    public String mark() {
        return PayChannelEnum.ALI_PAY.name();
    }

    public void execute(PayCallbackRequest requestParam) {
        callback(requestParam);
    }
}
