package com.aa03.index12306.biz.ticketservice.controller;


import com.aa03.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.aa03.index12306.biz.ticketservice.service.TicketService;
import com.aa03.index12306.framework.starter.convention.result.Result;
import com.aa03.index12306.framework.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 车票控制层
 */
@RestController
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    /**
     * 根据条件查询车票
     */
    @GetMapping("/api/ticket-service/ticket/query")
    public Result<TicketPageQueryRespDTO> pageListTicketQuery(TicketPageQueryReqDTO requestParam) {
        return Results.success(ticketService.pageListTicketQueryV1(requestParam));
    }

}
