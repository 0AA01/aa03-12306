package com.aa03.index12306.biz.ticketservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.aa03.index12306.biz.ticketservice.common.enums.TicketChainMarkEnum;
import com.aa03.index12306.biz.ticketservice.dao.entity.*;
import com.aa03.index12306.biz.ticketservice.dao.mapper.*;
import com.aa03.index12306.biz.ticketservice.dto.domain.SeatClassDTO;
import com.aa03.index12306.biz.ticketservice.dto.domain.TicketListDTO;
import com.aa03.index12306.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.aa03.index12306.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.aa03.index12306.biz.ticketservice.service.TicketService;
import com.aa03.index12306.biz.ticketservice.service.cache.SeatMarginCacheLoader;
import com.aa03.index12306.biz.ticketservice.toolkit.DateUtil;
import com.aa03.index12306.biz.ticketservice.toolkit.TimeStringComparator;
import com.aa03.index12306.framework.starter.cache.DistributedCache;
import com.aa03.index12306.framework.starter.cache.toolkit.CacheUtil;
import com.aa03.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.aa03.index12306.biz.ticketservice.common.constant.Index12306Constant.ADVANCE_TICKET_DAY;
import static com.aa03.index12306.biz.ticketservice.common.constant.RedisKeyConstant.*;
import static com.aa03.index12306.biz.ticketservice.toolkit.DateUtil.convertDateToLocalTime;

/**
 * 车票接口实现层
 */
@Service
@RequiredArgsConstructor
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService {

    private final RedissonClient redissonClient;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<TicketPageQueryReqDTO> ticketPageQueryAbstractChainContext;
    private final StationMapper stationMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final TrainMapper trainMapper;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final SeatMarginCacheLoader seatMarginCacheLoader;

    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        // 使用责任链进行数据合法验证
        ticketPageQueryAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(), requestParam);
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        // 查找出发地、目的地的地区code与站点映射缓存 （REGION_TRAIN_STATION_MAPPING，地区code，城市名称）
        List<Object> stationDetails = stringRedisTemplate.opsForHash()
                .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
        long count = stationDetails.stream().filter(Objects::isNull).count();
        // 出发地或目的地缓存查询为null，将出发地和目的地映射成地区
        if (count > 0) {
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION_MAPPING);
            lock.lock();
            try {
                // 双重判断缓存
                stationDetails = stringRedisTemplate.opsForHash()
                        .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
                count = stationDetails.stream().filter(Objects::isNull).count();
                if (count > 0) {
                    // 将所有 车站code 映射成 车站城市名称
                    List<StationDO> stationDOList = stationMapper.selectList(Wrappers.emptyWrapper());
                    Map<String, String> regionTrainStationMap = new HashMap<>();
                    stationDOList.forEach(each -> regionTrainStationMap.put(each.getCode(), each.getRegionName()));
                    stringRedisTemplate.opsForHash().putAll(REGION_TRAIN_STATION_MAPPING, regionTrainStationMap);
                    stationDetails = new ArrayList<>();
                    stationDetails.add(regionTrainStationMap.get(requestParam.getFromStation()));
                    stationDetails.add(regionTrainStationMap.get(requestParam.getToStation()));
                }
            } finally {
                lock.unlock();
            }
        }
        List<TicketListDTO> seatResults = new ArrayList<>();
        // （起始城市 + 终点城市，列车Id + 起始站点 + 终点站，TicketListDTO 车次集合实体）
        String buildRegionTrainStationHashKey = String.format(REGION_TRAIN_STATION, stationDetails.get(0), stationDetails.get(1));
        // 起始城市到终点城市的列车信息
        Map<Object, Object> regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
        if (MapUtil.isEmpty(regionTrainStationAllMap)) {
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION);
            lock.lock();
            try {
                regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
                // 双重判断缓存
                if (MapUtil.isEmpty(regionTrainStationAllMap)) {
                    // 根据起始城市、终点城市，查找所有列车站点关系的数据
                    LambdaQueryWrapper<TrainStationRelationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                            .eq(TrainStationRelationDO::getStartRegion, stationDetails.get(0))
                            .eq(TrainStationRelationDO::getEndRegion, stationDetails.get(1));
                    List<TrainStationRelationDO> trainStationRelationList = trainStationRelationMapper.selectList(queryWrapper);

                    // 转成列车信息放入缓存（列车id，列车数据）
                    for (TrainStationRelationDO each : trainStationRelationList) {
                        TrainDO trainDO = distributedCache.safeGet(
                                TRAIN_INFO + each.getTrainId(),
                                TrainDO.class,
                                () -> trainMapper.selectById(each.getTrainId()),
                                ADVANCE_TICKET_DAY,
                                TimeUnit.DAYS);
                        TicketListDTO result = new TicketListDTO();
                        result.setTrainId(String.valueOf(trainDO.getId()));
                        result.setTrainNumber(trainDO.getTrainNumber());
                        result.setDepartureTime(convertDateToLocalTime(each.getDepartureTime(), "HH:mm"));
                        result.setArrivalTime(convertDateToLocalTime(each.getArrivalTime(), "HH:mm"));
                        result.setDuration(DateUtil.calculateHourDifference(each.getDepartureTime(), each.getArrivalTime()));
                        result.setDeparture(each.getDeparture());
                        result.setArrival(each.getArrival());
                        result.setDepartureFlag(each.getDepartureFlag());
                        result.setArrivalFlag(each.getArrivalFlag());
                        result.setTrainType(trainDO.getTrainType());
                        result.setTrainBrand(trainDO.getTrainBrand());
                        if (StrUtil.isNotBlank(trainDO.getTrainTag())) {
                            result.setTrainTags(StrUtil.split(trainDO.getTrainTag(), ","));
                        }
                        long betweenDay = cn.hutool.core.date.DateUtil.betweenDay(each.getDepartureTime(), each.getArrivalTime(), false);
                        result.setDaysArrived((int) betweenDay);
                        result.setSaleStatus(new Date().after(trainDO.getSaleTime()) ? 0 : 1);
                        result.setSaleTime(convertDateToLocalTime(trainDO.getSaleTime(), "MM-dd HH:mm"));
                        seatResults.add(result);
                        // 所有符合条件的放入集合、批处理
                        regionTrainStationAllMap.put(CacheUtil.buildKey(String.valueOf(each.getTrainId()), each.getDeparture(), each.getArrival()), JSON.toJSONString(result));
                    }
                    stringRedisTemplate.opsForHash().putAll(buildRegionTrainStationHashKey, regionTrainStationAllMap);
                }
            } finally {
                lock.unlock();
            }
        }
        // 车次信息List
        seatResults = CollUtil.isEmpty(seatResults)
                ? regionTrainStationAllMap.values().stream().map(each -> JSON.parseObject(each.toString(), TicketListDTO.class)).toList()
                : seatResults;
        // 按照出发时间排序
        seatResults = seatResults.stream().sorted(new TimeStringComparator()).toList();

        // 将上述符合条件列车信息，根据trainId、出发地、目的地查出价格，将所有不同座位价格信息进行缓存
        for (TicketListDTO each : seatResults) {
            // （列车id + 起始站点 + 终点站，List<TrainStationPriceDO 列车站点价格实体（不同座位类型）>）
            String trainStationPriceStr = distributedCache.safeGet(
                    String.format(TRAIN_STATION_PRICE, each.getTrainId(), each.getDeparture(), each.getArrival()),
                    String.class,
                    () -> {
                        LambdaQueryWrapper<TrainStationPriceDO> trainStationPriceQueryWrapper = Wrappers.lambdaQuery(TrainStationPriceDO.class)
                                .eq(TrainStationPriceDO::getDeparture, each.getDeparture())
                                .eq(TrainStationPriceDO::getArrival, each.getArrival())
                                .eq(TrainStationPriceDO::getTrainId, each.getTrainId());
                        return JSON.toJSONString(trainStationPriceMapper.selectList(trainStationPriceQueryWrapper));
                    },
                    ADVANCE_TICKET_DAY,
                    TimeUnit.DAYS
            );
            // 查找出该列车（s -> e）的不同席别类型的数量 缓存（列车id + s + e，席别类型，席别数量）
            List<TrainStationPriceDO> trainStationPriceDOList = JSON.parseArray(trainStationPriceStr, TrainStationPriceDO.class);
            List<SeatClassDTO> seatClassList = new ArrayList<>();
            trainStationPriceDOList.forEach(item -> {
                String seatType = String.valueOf(item.getSeatType());
                String keySuffix = StrUtil.join("_", each.getTrainId(), item.getDeparture(), item.getArrival());
                Object quantityObj = stringRedisTemplate.opsForHash().get(TRAIN_STATION_REMAINING_TICKET + keySuffix, seatType);
                int quantity = Optional.ofNullable(quantityObj)
                        .map(Object::toString)
                        .map(Integer::parseInt)
                        .orElseGet(() -> {
                            Map<String, String> seatMarginMap = seatMarginCacheLoader.load(String.valueOf(each.getTrainId()), seatType, item.getDeparture(), item.getArrival());
                            return Optional.ofNullable(seatMarginMap.get(String.valueOf(item.getSeatType()))).map(Integer::parseInt).orElse(0);
                        });
                seatClassList.add(new SeatClassDTO(item.getSeatType(), quantity, new BigDecimal(item.getPrice()).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP), false));
            });
            each.setSeatClassList(seatClassList);
        }
        return TicketPageQueryRespDTO.builder()
                .trainList(seatResults)
                .departureStationList(buildDepartureStationList(seatResults))
                .arrivalStationList(buildArrivalStationList(seatResults))
                .trainBrandList(buildTrainBrandList(seatResults))
                .seatClassTypeList(buildSeatClassList(seatResults))
                .build();
    }

    private List<String> buildDepartureStationList(List<TicketListDTO> seatResults) {
        return seatResults.stream().map(TicketListDTO::getDeparture).distinct().collect(Collectors.toList());
    }

    private List<String> buildArrivalStationList(List<TicketListDTO> seatResults) {
        return seatResults.stream().map(TicketListDTO::getArrival).distinct().collect(Collectors.toList());
    }

    private List<Integer> buildSeatClassList(List<TicketListDTO> seatResults) {
        Set<Integer> resultSeatClassList = new HashSet<>();
        for (TicketListDTO each : seatResults) {
            for (SeatClassDTO item : each.getSeatClassList()) {
                resultSeatClassList.add(item.getType());
            }
        }
        return resultSeatClassList.stream().toList();
    }

    private List<Integer> buildTrainBrandList(List<TicketListDTO> seatResults) {
        Set<Integer> trainBrandSet = new HashSet<>();
        for (TicketListDTO each : seatResults) {
            if (StrUtil.isNotBlank(each.getTrainBrand())) {
                trainBrandSet.addAll(StrUtil.split(each.getTrainBrand(), ",").stream().map(Integer::parseInt).toList());
            }
        }
        return trainBrandSet.stream().toList();
    }

    private final ScheduledExecutorService tokenIsNullRefreshExecutor = Executors.newScheduledThreadPool(1);

//    private void tokenIsNullRefreshToken(PurchaseTicketReqDTO requestParam, TokenResultDTO tokenResult) {
//        RLock lock = redissonClient.getLock(String.format(LOCK_TOKEN_BUCKET_ISNULL, requestParam.getTrainId()));
//        if (!lock.tryLock()) {
//            return;
//        }
//        tokenIsNullRefreshExecutor.schedule(() -> {
//            try {
//                List<Integer> seatTypes = new ArrayList<>();
//                Map<Integer, Integer> tokenCountMap = new HashMap<>();
//                tokenResult.getTokenIsNullSeatTypeCounts().stream()
//                        .map(each -> each.split("_"))
//                        .forEach(split -> {
//                            int seatType = Integer.parseInt(split[0]);
//                            seatTypes.add(seatType);
//                            tokenCountMap.put(seatType, Integer.parseInt(split[1]));
//                        });
//                List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(Long.parseLong(requestParam.getTrainId()), requestParam.getDeparture(), requestParam.getArrival(), seatTypes);
//                for (SeatTypeCountDTO each : seatTypeCountDTOList) {
//                    Integer tokenCount = tokenCountMap.get(each.getSeatType());
//                    if (tokenCount < each.getSeatCount()) {
//                        ticketAvailabilityTokenBucket.delTokenInBucket(requestParam);
//                        break;
//                    }
//                }
//            } finally {
//                lock.unlock();
//            }
//        }, 10, TimeUnit.SECONDS);
//    }
}
