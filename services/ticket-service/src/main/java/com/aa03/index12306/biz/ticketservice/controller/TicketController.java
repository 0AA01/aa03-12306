package com.aa03.index12306.biz.ticketservice.controller;


import com.aa03.index12306.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPurchaseRespDTO;
import com.aa03.index12306.biz.ticketservice.service.TicketService;
import com.aa03.index12306.framework.starter.convention.result.Result;
import com.aa03.index12306.framework.starter.idempotent.annotation.Idempotent;
import com.aa03.index12306.framework.starter.idempotent.enums.IdempotentSceneEnum;
import com.aa03.index12306.framework.starter.idempotent.enums.IdempotentTypeEnum;
import com.aa03.index12306.framework.starter.log.annotation.ILog;
import com.aa03.index12306.framework.starter.web.Results;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    /**
     * 购买车票
     */
    @ILog
    @Idempotent(
            uniqueKeyPrefix = "index12306-ticket:lock_purchase-tickets:",
            key = "T(com.aa03.index12306.framework.starter.bases.ApplicationContextHolder).getBean('environment').getProperty('unique-name', '')"
                    + "+'_'+"
                    + "T(com.aa03.index12306.frameworks.starter.user.core.UserContext).getUsername()",
            message = "正在执行下单流程，请稍后...",
            scene = IdempotentSceneEnum.RESTAPI,
            type = IdempotentTypeEnum.SPEL
    )
    @PostMapping("/api/ticket-service/ticket/purchase")
    public Result<TicketPurchaseRespDTO> purchaseTickets(@RequestBody PurchaseTicketReqDTO requestParam) {
        return Results.success(ticketService.purchaseTicketsV1(requestParam));
    }

}
