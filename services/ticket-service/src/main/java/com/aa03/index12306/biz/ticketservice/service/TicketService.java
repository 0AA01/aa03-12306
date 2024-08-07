package com.aa03.index12306.biz.ticketservice.service;

import com.aa03.index12306.biz.ticketservice.dao.entity.TicketDO;
import com.aa03.index12306.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPurchaseRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 车票接口层
 */
public interface TicketService extends IService<TicketDO> {

    /**
     * 根据条件分页查询车票
     *
     * @param requestParam 分页查询车票请求参数
     * @return 查询车票返回结果
     */
    TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam);

    /**
     * 购买车票
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam);

    /**
     * 执行购买车票
     * 被对应购票版本号接口调用 {@link TicketService#purchaseTicketsV1(PurchaseTicketReqDTO)} and {@link TicketService#purchaseTicketsV2(PurchaseTicketReqDTO)}
     *
     * @param requestParam 车票购买请求参数
     * @return 订单号
     */
    TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam);
}
