package com.aa03.index12306.biz.orderservice.service.impl;

import com.aa03.index12306.biz.orderservice.dao.entity.OrderItemDO;
import com.aa03.index12306.biz.orderservice.dao.mapper.OrderItemMapper;
import com.aa03.index12306.biz.orderservice.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单明细接口层实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemDO> implements OrderItemService {
}
