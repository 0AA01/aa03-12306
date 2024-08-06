package com.aa03.index12306.biz.orderservice.service;

import com.aa03.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderPageQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderSelfPageQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderDetailSelfRespDTO;
import com.aa03.index12306.framework.starter.convention.page.PageResponse;

/**
 * 订单接口层
 */
public interface OrderService {

    /**
     * 跟据订单号查询车票订单
     *
     * @param orderSn 订单号
     * @return 订单详情
     */
    TicketOrderDetailRespDTO queryTicketOrderByOrderSn(String orderSn);

    /**
     * 跟据用户名分页查询车票订单
     *
     * @param requestParam 跟据用户 ID 分页查询对象
     * @return 订单分页详情
     */
    PageResponse<TicketOrderDetailRespDTO> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam);

    /**
     * 查询本人车票订单
     *
     * @param requestParam 请求参数
     * @return 本人车票订单集合
     */
    PageResponse<TicketOrderDetailSelfRespDTO> pageSelfTicketOrder(TicketOrderSelfPageQueryReqDTO requestParam);

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
