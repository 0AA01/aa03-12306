package com.aa03.index12306.biz.ticketservice.service.handler.ticket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.lang.Pair;
import com.aa03.index12306.biz.ticketservice.common.enums.VehicleSeatTypeEnum;
import com.aa03.index12306.biz.ticketservice.common.enums.VehicleTypeEnum;
import com.aa03.index12306.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import com.aa03.index12306.biz.ticketservice.dto.domain.TrainSeatBaseDTO;
import com.aa03.index12306.biz.ticketservice.service.SeatService;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.base.AbstractTrainPurchaseTicketTemplate;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.base.BitMapCheckSeat;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.base.BitMapCheckSeatStatusFactory;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.dto.SelectSeatDTO;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import com.aa03.index12306.biz.ticketservice.service.handler.ticket.select.SeatSelection;
import com.aa03.index12306.biz.ticketservice.toolkit.CarriageVacantSeatCalculateUtil;
import com.aa03.index12306.biz.ticketservice.toolkit.SeatNumberUtil;
import com.aa03.index12306.framework.starter.convention.exception.ServiceException;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.aa03.index12306.biz.ticketservice.toolkit.ChooseSeatUtil.TRAIN_BUSINESS;

/**
 * 高铁商务座购票组件
 */
@Component
@RequiredArgsConstructor
public class TrainBusinessClassPurchaseTicketHandler extends AbstractTrainPurchaseTicketTemplate {

    private final SeatService seatService;

    private static final Map<Character, Integer> SEAT_Y_INT = Map.of('A', 0, 'C', 1, 'F', 2);

    @Override
    public String mark() {
        return VehicleTypeEnum.HIGH_SPEED_RAIN.getName() + VehicleSeatTypeEnum.BUSINESS_CLASS.getName();
    }

    @Override
    protected List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        // 需要购买车票的乘客
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        // 查询列车有余票的车厢号集合
        List<String> trainCarriageList = seatService.listUsableCarriageNumber(trainId, requestParam.getSeatType(), departure, arrival);
        // 获取列车车厢余票集合
        List<Integer> trainStationCarriageRemainingTicket = seatService.listSeatRemainingTicket(trainId, departure, arrival, trainCarriageList);
        int remainingTicketSum = trainStationCarriageRemainingTicket.stream().mapToInt(Integer::intValue).sum();
        if (remainingTicketSum < passengerSeatDetails.size()) {
            throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }
        if (passengerSeatDetails.size() < 3) {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())) {
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            return selectSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
        } else {
            if (CollUtil.isNotEmpty(requestParam.getRequestParam().getChooseSeats())) {
                Pair<List<TrainPurchaseTicketRespDTO>, Boolean> actualSeatPair = findMatchSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
                return actualSeatPair.getKey();
            }
            return selectComplexSeats(requestParam, trainCarriageList, trainStationCarriageRemainingTicket);
        }
    }

    private Pair<List<TrainPurchaseTicketRespDTO>, Boolean> findMatchSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        TrainSeatBaseDTO trainSeatBaseDTO = buildTrainSeatBaseDTO(requestParam);
        int chooseSeatSize = trainSeatBaseDTO.getChooseSeatList().size();
        List<TrainPurchaseTicketRespDTO> actualResult = Lists.newArrayListWithCapacity(trainSeatBaseDTO.getPassengerSeatDetails().size());
        // 获取验证座位实体类
        BitMapCheckSeat instance = BitMapCheckSeatStatusFactory.getInstance(TRAIN_BUSINESS);
        HashMap<String, List<Pair<Integer, Integer>>> carriagesSeatMap = new HashMap<>(4);
        int passengersNumber = trainSeatBaseDTO.getPassengerSeatDetails().size();
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            // 获取车厢号
            String carriagesNumber = trainCarriageList.get(i);
            // 根据车厢号获取可用的座位集合
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainSeatBaseDTO.getTrainId(), carriagesNumber, requestParam.getSeatType(), trainSeatBaseDTO.getDeparture(), trainSeatBaseDTO.getArrival());
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            // 统计空余座位
            List<Pair<Integer, Integer>> vacantSeatList = CarriageVacantSeatCalculateUtil.buildCarriageVacantSeatList2(actualSeats, 2, 3);
            // 查看选择座位是否存在
            boolean isExists = instance.checkChooseSeat(trainSeatBaseDTO.getChooseSeatList(), actualSeats, SEAT_Y_INT);
            long vacantSeatCount = vacantSeatList.size();
            List<Pair<Integer, Integer>> sureSeatList = new ArrayList<>();
            List<String> selectSeats = Lists.newArrayListWithCapacity(passengersNumber);
            boolean flag = false;
            // 指定座位存在，并且该车厢余票数量 >= 购票人数量
            if (isExists && vacantSeatCount >= passengersNumber) {
                Iterator<Pair<Integer, Integer>> pairIterator = vacantSeatList.iterator();
                for (int i1 = 0; i1 < chooseSeatSize; i1++) {
                    // 选择特定座位信息数量为1
                    if (chooseSeatSize == 1) {
                        String chooseSeat = trainSeatBaseDTO.getChooseSeatList().get(i1);
                        int seatX = Integer.parseInt(chooseSeat.substring(1));
                        int seatY = SEAT_Y_INT.get(chooseSeat.charAt(0));
                        // 选择该座位，删除该座位为空的信息
                        if (actualSeats[seatX][seatY] == 0) {
                            sureSeatList.add(new Pair<>(seatX, seatY));
                            while (pairIterator.hasNext()) {
                                Pair<Integer, Integer> pair = pairIterator.next();
                                if (pair.getKey() == seatX && pair.getValue() == seatY) {
                                    pairIterator.remove();
                                    break;
                                }
                            }
                        } else {
                            if (actualSeats[1][seatY] == 0) {
                                // 选择该座位，删除该座位为空的信息
                                sureSeatList.add(new Pair<>(1, seatY));
                                while (pairIterator.hasNext()) {
                                    Pair<Integer, Integer> pair = pairIterator.next();
                                    if (pair.getKey() == 1 && pair.getValue() == seatY) {
                                        pairIterator.remove();
                                        break;
                                    }
                                }
                            } else {
                                flag = true;
                            }
                        }
                    } else {
                        String chooseSeat = trainSeatBaseDTO.getChooseSeatList().get(i1);
                        int seatX = Integer.parseInt(chooseSeat.substring(1));
                        int seatY = SEAT_Y_INT.get(chooseSeat.charAt(0));
                        if (actualSeats[seatX][seatY] == 0) {
                            // 选择该座位，删除该座位为空的信息
                            sureSeatList.add(new Pair<>(seatX, seatY));
                            while (pairIterator.hasNext()) {
                                Pair<Integer, Integer> pair = pairIterator.next();
                                if (pair.getKey() == seatX && pair.getValue() == seatY) {
                                    pairIterator.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
                // 如果车厢能满足要求或者不能满足要求并且是最后一个车厢，就执行下面语句，否则continue
                if (flag && i < trainStationCarriageRemainingTicket.size() - 1) {
                    continue;
                }
                // 已选择座位的数据 != 所有乘客数量
                if (sureSeatList.size() != passengersNumber) {
                    int needSeatSize = passengersNumber - sureSeatList.size();
                    // 直接从剩余座位里面选择差值
                    sureSeatList.addAll(vacantSeatList.subList(0, needSeatSize));
                }
                // 将所有选择的座位记录下来
                for (Pair<Integer, Integer> each : sureSeatList) {
                    selectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, (each.getValue() + 1)));
                }
                // 将选择的座位信息和乘车对应后，返回
                AtomicInteger countNum = new AtomicInteger(0);
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(carriagesNumber);
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
                return new Pair<>(actualResult, Boolean.TRUE);
            } else {
                if (i < trainStationCarriageRemainingTicket.size()) {
                    // 将每个车厢的余票信息记录下来
                    if (vacantSeatCount > 0) {
                        carriagesSeatMap.put(carriagesNumber, vacantSeatList);
                    }
                    // 只有最后一个车厢也不满足购票条件，才选择分车厢购票
                    if (i == trainStationCarriageRemainingTicket.size() - 1) {
                        // 找到一个车厢剩余座位数量 >= 乘客数量
                        Pair<String, List<Pair<Integer, Integer>>> findSureCarriage = null;
                        for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                            if (entry.getValue().size() >= passengersNumber) {
                                findSureCarriage = new Pair<>(entry.getKey(), entry.getValue().subList(0, passengersNumber));
                                break;
                            }
                        }
                        if (null != findSureCarriage) {
                            // 直接选择这个车厢的剩余的座位数量
                            sureSeatList = findSureCarriage.getValue().subList(0, passengersNumber);
                            for (Pair<Integer, Integer> each : sureSeatList) {
                                selectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                            }
                            AtomicInteger countNum = new AtomicInteger(0);
                            for (String selectSeat : selectSeats) {
                                TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                result.setSeatNumber(selectSeat);
                                result.setSeatType(currentTicketPassenger.getSeatType());
                                result.setCarriageNumber(findSureCarriage.getKey());
                                result.setPassengerId(currentTicketPassenger.getPassengerId());
                                actualResult.add(result);
                            }
                        } else {
                            int sureSeatListSize = 0;
                            AtomicInteger countNum = new AtomicInteger(0);
                            // 依次选择每个车厢的剩余数量，直到选满
                            for (Map.Entry<String, List<Pair<Integer, Integer>>> entry : carriagesSeatMap.entrySet()) {
                                if (sureSeatListSize < passengersNumber) {
                                    if (sureSeatListSize + entry.getValue().size() < passengersNumber) {
                                        sureSeatListSize = sureSeatListSize + entry.getValue().size();
                                        List<String> actualSelectSeats = new ArrayList<>();
                                        for (Pair<Integer, Integer> each : entry.getValue()) {
                                            actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                        }
                                        for (String selectSeat : actualSelectSeats) {
                                            TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                            PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                            result.setSeatNumber(selectSeat);
                                            result.setSeatType(currentTicketPassenger.getSeatType());
                                            result.setCarriageNumber(entry.getKey());
                                            result.setPassengerId(currentTicketPassenger.getPassengerId());
                                            actualResult.add(result);
                                        }
                                    } else {
                                        int needSeatSize = entry.getValue().size() - (sureSeatListSize + entry.getValue().size() - passengersNumber);
                                        sureSeatListSize = sureSeatListSize + needSeatSize;
                                        if (sureSeatListSize >= passengersNumber) {
                                            List<String> actualSelectSeats = new ArrayList<>();
                                            for (Pair<Integer, Integer> each : entry.getValue().subList(0, needSeatSize)) {
                                                actualSelectSeats.add("0" + (each.getKey() + 1) + SeatNumberUtil.convert(0, each.getValue() + 1));
                                            }
                                            for (String selectSeat : actualSelectSeats) {
                                                TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                                                PurchaseTicketPassengerDetailDTO currentTicketPassenger = trainSeatBaseDTO.getPassengerSeatDetails().get(countNum.getAndIncrement());
                                                result.setSeatNumber(selectSeat);
                                                result.setSeatType(currentTicketPassenger.getSeatType());
                                                result.setCarriageNumber(entry.getKey());
                                                result.setPassengerId(currentTicketPassenger.getPassengerId());
                                                actualResult.add(result);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        return new Pair<>(actualResult, Boolean.TRUE);
                    }
                }
            }
        }
        return new Pair<>(null, Boolean.FALSE);
    }

    private List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        String carriagesNumber;
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            carriagesNumber = trainCarriageList.get(i);
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            int[][] select = SeatSelection.adjacent(passengerSeatDetails.size(), actualSeats);
            if (select != null) {
                carriagesNumberSeatsMap.put(carriagesNumber, select);
                break;
            }
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
            if (i < trainStationCarriageRemainingTicket.size() - 1) {
                continue;
            }
            // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        select = nonAdjacentSeats;
                        carriagesNumberSeatsMap.put(carriagesNumberBack, select);
                        break;
                    }
                }
            }
            // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
            if (Objects.isNull(select)) {
                for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                    String carriagesNumberBack = entry.getKey();
                    int demotionStockNumBack = entry.getValue();
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(demotionStockNumBack, seats);
                    carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
                }
            }
        }
        // 乘车人员在单一车厢座位不满足，触发乘车人元分布在不同车厢
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }

    private List<TrainPurchaseTicketRespDTO> selectComplexSeats(SelectSeatDTO requestParam, List<String> trainCarriageList, List<Integer> trainStationCarriageRemainingTicket) {
        String trainId = requestParam.getRequestParam().getTrainId();
        String departure = requestParam.getRequestParam().getDeparture();
        String arrival = requestParam.getRequestParam().getArrival();
        List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails = requestParam.getPassengerSeatDetails();
        List<TrainPurchaseTicketRespDTO> actualResult = new ArrayList<>();
        Map<String, Integer> demotionStockNumMap = new LinkedHashMap<>();
        Map<String, int[][]> actualSeatsMap = new HashMap<>();
        Map<String, int[][]> carriagesNumberSeatsMap = new HashMap<>();
        String carriagesNumber;
        // 多人分配同一车厢邻座
        for (int i = 0; i < trainStationCarriageRemainingTicket.size(); i++) {
            carriagesNumber = trainCarriageList.get(i);
            List<String> listAvailableSeat = seatService.listAvailableSeat(trainId, carriagesNumber, requestParam.getSeatType(), departure, arrival);
            int[][] actualSeats = new int[2][3];
            for (int j = 1; j < 3; j++) {
                for (int k = 1; k < 4; k++) {
                    // 当前默认按照复兴号商务座排序，后续这里需要按照简单工厂对车类型进行获取 y 轴
                    actualSeats[j - 1][k - 1] = listAvailableSeat.contains("0" + j + SeatNumberUtil.convert(0, k)) ? 0 : 1;
                }
            }
            int[][] actualSeatsTranscript = deepCopy(actualSeats);
            List<int[][]> actualSelects = new ArrayList<>();
            List<List<PurchaseTicketPassengerDetailDTO>> splitPassengerSeatDetails = ListUtil.split(passengerSeatDetails, 2);
            for (List<PurchaseTicketPassengerDetailDTO> each : splitPassengerSeatDetails) {
                int[][] select = SeatSelection.adjacent(each.size(), actualSeatsTranscript);
                if (select != null) {
                    for (int[] ints : select) {
                        actualSeatsTranscript[ints[0] - 1][ints[1] - 1] = 1;
                    }
                    actualSelects.add(select);
                }
            }
            if (actualSelects.size() == splitPassengerSeatDetails.size()) {
                int[][] actualSelect = null;
                for (int j = 0; j < actualSelects.size(); j++) {
                    if (j == 0) {
                        actualSelect = mergeArrays(actualSelects.get(j), actualSelects.get(j + 1));
                    }
                    if (j != 0 && actualSelects.size() > 2) {
                        actualSelect = mergeArrays(actualSelect, actualSelects.get(j + 1));
                    }
                }
                carriagesNumberSeatsMap.put(carriagesNumber, actualSelect);
                break;
            }
            int demotionStockNum = 0;
            for (int[] actualSeat : actualSeats) {
                for (int i1 : actualSeat) {
                    if (i1 == 0) {
                        demotionStockNum++;
                    }
                }
            }
            demotionStockNumMap.putIfAbsent(carriagesNumber, demotionStockNum);
            actualSeatsMap.putIfAbsent(carriagesNumber, actualSeats);
        }
        // 如果邻座算法无法匹配，尝试对用户进行降级分配：同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                if (demotionStockNumBack > passengerSeatDetails.size()) {
                    int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                    int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(passengerSeatDetails.size(), seats);
                    if (Objects.equals(nonAdjacentSeats.length, passengerSeatDetails.size())) {
                        carriagesNumberSeatsMap.put(carriagesNumberBack, nonAdjacentSeats);
                        break;
                    }
                }
            }
        }
        // 如果同车厢也已无法匹配，则对用户座位再次降级：不同车厢不邻座
        if (CollUtil.isEmpty(carriagesNumberSeatsMap)) {
            int undistributedPassengerSize = passengerSeatDetails.size();
            for (Map.Entry<String, Integer> entry : demotionStockNumMap.entrySet()) {
                String carriagesNumberBack = entry.getKey();
                int demotionStockNumBack = entry.getValue();
                int[][] seats = actualSeatsMap.get(carriagesNumberBack);
                int[][] nonAdjacentSeats = SeatSelection.nonAdjacent(Math.min(undistributedPassengerSize, demotionStockNumBack), seats);
                undistributedPassengerSize = undistributedPassengerSize - demotionStockNumBack;
                carriagesNumberSeatsMap.put(entry.getKey(), nonAdjacentSeats);
            }
        }
        // 乘车人员在单一车厢座位不满足，触发乘车人元分布在不同车厢
        int count = (int) carriagesNumberSeatsMap.values().stream()
                .flatMap(Arrays::stream)
                .count();
        if (CollUtil.isNotEmpty(carriagesNumberSeatsMap) && passengerSeatDetails.size() == count) {
            int countNum = 0;
            for (Map.Entry<String, int[][]> entry : carriagesNumberSeatsMap.entrySet()) {
                List<String> selectSeats = new ArrayList<>();
                for (int[] ints : entry.getValue()) {
                    selectSeats.add("0" + ints[0] + SeatNumberUtil.convert(0, ints[1]));
                }
                for (String selectSeat : selectSeats) {
                    TrainPurchaseTicketRespDTO result = new TrainPurchaseTicketRespDTO();
                    PurchaseTicketPassengerDetailDTO currentTicketPassenger = passengerSeatDetails.get(countNum++);
                    result.setSeatNumber(selectSeat);
                    result.setSeatType(currentTicketPassenger.getSeatType());
                    result.setCarriageNumber(entry.getKey());
                    result.setPassengerId(currentTicketPassenger.getPassengerId());
                    actualResult.add(result);
                }
            }
        }
        return actualResult;
    }

    public static int[][] mergeArrays(int[][] array1, int[][] array2) {
        List<int[]> list = new ArrayList<>(Arrays.asList(array1));
        list.addAll(Arrays.asList(array2));
        return list.toArray(new int[0][]);
    }

    public static int[][] deepCopy(int[][] originalArray) {
        int[][] copy = new int[originalArray.length][originalArray[0].length];
        for (int i = 0; i < originalArray.length; i++) {
            System.arraycopy(originalArray[i], 0, copy[i], 0, originalArray[i].length);
        }
        return copy;
    }
}