package com.aa03.index12306.biz.orderservice.service;

import com.aa03.index12306.biz.orderservice.dao.entity.OrderItemDO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderItemQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 订单明细接口层
 */
public interface OrderItemService extends IService<OrderItemDO> {

    /**
     * 根据子订单记录id查询车票子订单详情
     *
     * @param requestParam 请求参数
     */
    List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam);
}
