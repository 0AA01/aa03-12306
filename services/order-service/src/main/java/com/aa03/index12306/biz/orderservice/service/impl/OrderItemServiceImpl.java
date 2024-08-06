package com.aa03.index12306.biz.orderservice.service.impl;

import com.aa03.index12306.biz.orderservice.dao.entity.OrderItemDO;
import com.aa03.index12306.biz.orderservice.dao.mapper.OrderItemMapper;
import com.aa03.index12306.biz.orderservice.dto.req.TicketOrderItemQueryReqDTO;
import com.aa03.index12306.biz.orderservice.dto.resp.TicketOrderPassengerDetailRespDTO;
import com.aa03.index12306.biz.orderservice.service.OrderItemService;
import com.aa03.index12306.framework.starter.common.toolkit.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单明细接口层实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements OrderItemService {

    private final OrderItemMapper orderItemMapper;

    @Override
    public List<TicketOrderPassengerDetailRespDTO> queryTicketItemOrderById(TicketOrderItemQueryReqDTO requestParam) {
        LambdaQueryWrapper<OrderItemDO> queryWrapper = Wrappers.lambdaQuery(OrderItemDO.class)
                .eq(OrderItemDO::getOrderSn, requestParam.getOrderSn())
                .in(OrderItemDO::getId, requestParam.getOrderItemRecordIds());
        List<OrderItemDO> orderItemDOList = orderItemMapper.selectList(queryWrapper);
        return BeanUtil.convert(orderItemDOList, TicketOrderPassengerDetailRespDTO.class);
    }
}
