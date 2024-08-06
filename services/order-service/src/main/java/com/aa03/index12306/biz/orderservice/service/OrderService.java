package com.aa03.index12306.biz.orderservice.service;

import com.aa03.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;

/**
 * 订单接口层
 */
public interface OrderService {

    /**
     * 创建火车票订单
     *
     * @param requestParam 商品订单入参
     * @return 订单号
     */
    String createTicketOrder(TicketOrderCreateReqDTO requestParam);

    /**
     * 关闭火车票订单
     *
     * @param requestParam 关闭火车票订单入参
     */
    Boolean closeTickOrder(CancelTicketOrderReqDTO requestParam);

    /**
     * 取消火车票订单
     *
     * @param requestParam 取消火车票订单入参
     */
    boolean cancelTickOrder(CancelTicketOrderReqDTO requestParam);
}
