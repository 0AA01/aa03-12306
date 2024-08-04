package com.aa03.index12306.biz.ticketservice.service;

import com.aa03.index12306.biz.ticketservice.dao.entity.TicketDO;
import com.aa03.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
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
}
