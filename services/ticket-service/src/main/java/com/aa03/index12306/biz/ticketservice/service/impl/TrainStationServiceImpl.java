package com.aa03.index12306.biz.ticketservice.service.impl;

import com.aa03.index12306.biz.ticketservice.dao.entity.TrainStationDO;
import com.aa03.index12306.biz.ticketservice.dao.mapper.TrainStationMapper;
import com.aa03.index12306.biz.ticketservice.dto.domain.RouteDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TrainStationQueryRespDTO;
import com.aa03.index12306.biz.ticketservice.service.TrainStationService;
import com.aa03.index12306.biz.ticketservice.toolkit.StationCalculateUtil;
import com.aa03.index12306.framework.starter.common.toolkit.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 列车站点接口实现层
 */
@Service
@RequiredArgsConstructor
public class TrainStationServiceImpl implements TrainStationService {

    private final TrainStationMapper trainStationMapper;

    @Override
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId);
        List<TrainStationDO> trainStationDOList = trainStationMapper.selectList(queryWrapper);
        return BeanUtil.convert(trainStationDOList, TrainStationQueryRespDTO.class);
    }

    @Override
    public List<RouteDTO> listTrainStationRoute(String trainId, String departure, String arrival) {
        // 根据列车id返回所有列车出发点
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId)
                .select(TrainStationDO::getDeparture);
        List<TrainStationDO> trainStationDOList = trainStationMapper.selectList(queryWrapper);
        List<String> trainStationAllList = trainStationDOList.stream().map(TrainStationDO::getDeparture).collect(Collectors.toList());
        // 计算出发站和终点站中间的站点（包含出发站和终点站）
        return StationCalculateUtil.throughStation(trainStationAllList, departure, arrival);
    }

    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId)
                .select(TrainStationDO::getDeparture);
        List<TrainStationDO> trainStationDOList = trainStationMapper.selectList(queryWrapper);
        List<String> trainStationAllList = trainStationDOList.stream().map(TrainStationDO::getDeparture).collect(Collectors.toList());
        return StationCalculateUtil.takeoutStation(trainStationAllList, departure, arrival);
    }
}
