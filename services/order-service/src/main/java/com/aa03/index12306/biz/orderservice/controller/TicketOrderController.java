package com.aa03.index12306.biz.orderservice.controller;

import cn.crane4j.annotation.AutoOperate;
import com.aa03.index12306.biz.orderservice.dto.req.CancelTicketOrderReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderCreateReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderItemQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderPageQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderDetailRespDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import com.aa03.index12306.biz.orderservice.service.OrderItemService;
import com.aa03.index12306.biz.orderservice.service.OrderService;
import com.aa03.index12306.framework.starter.convention.page.PageResponse;
import com.aa03.index12306.framework.starter.convention.result.Result;
import com.aa03.index12306.framework.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车票订单接口控制层
 */
@RestController
@RequiredArgsConstructor
public class TicketOrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    /**
     * 根据订单号查询车票订单
     */
    @GetMapping("/api/order-service/order/ticket/query")
    public Result<TicketOrderDetailRespDTO> queryTicketOrderByOrderSn(@RequestParam(value = "orderSn") String orderSn) {
        return Results.success(orderService.queryTicketOrderByOrderSn(orderSn));
    }

    /**
     * 根据子订单记录id查询车票子订单详情
     */
    @GetMapping("/api/order-service/order/item/ticket/query")
    public Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        return Results.success(orderItemService.queryTicketItemOrderById(requestParam));
    }

    /**
     * 分页查询车票订单
     */
    @AutoOperate(type = TicketOrderDetailRespDTO.class, on = "data.records")
    @GetMapping("/api/order-service/order/ticket/page")
    public Result<PageResponse<TicketOrderDetailRespDTO>> pageTicketOrder(TicketOrderPageQueryReqDTO requestParam) {
        return Results.success(orderService.pageTicketOrder(requestParam));
    }



    /**
     * 车票订单创建
     */
    @PostMapping("/api/order-service/order/ticket/create")
    public Result<String> createTicketOrder(@RequestBody TicketOrderCreateReqDTO requestParam) {
        return Results.success(orderService.createTicketOrder(requestParam));
    }

    /**
     * 车票订单关闭
     */
    @PostMapping("/api/order-service/order/ticket/close")
    public Result<Boolean> closeTickOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        return Results.success(orderService.closeTickOrder(requestParam));
    }

    /**
     * 车票订单取消
     */
    @PostMapping("/api/order-service/order/ticket/cancel")
    public Result<Boolean> cancelTickOrder(@RequestBody CancelTicketOrderReqDTO requestParam) {
        return Results.success(orderService.cancelTickOrder(requestParam));
    }
}
