package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //根据传入的日期区间统计每天的营业额，缺陷：如果某天没有开业或者没有营业额，就无法统计到当天的营业额
        // 因此只能一天一天统计营业额
        /*List<DayStatistics> dayStatistics = orderMapper.sumByBeginAndEnd(begin, end);
        StringJoiner days = new StringJoiner(",");
        StringJoiner Statistics = new StringJoiner(",");
        for (DayStatistics dayStatistic : dayStatistics) {
            days.add(dayStatistic.getDate().toString());
            Statistics.add(dayStatistic.getStatistics().toString());
        }*/

        //根据日期统计营业额，要计算出区间内所有的日期
        List<LocalDate> Dates = new ArrayList<>();
        List<Double> turnovers = new ArrayList<>();
        while (!begin.isAfter(end)) {
            Double d = orderMapper.sumByDate(begin);
            if (d == null) {
                d = 0.0;
            }
            Dates.add(begin);
            turnovers.add(d);
            begin = begin.plusDays(1);
        }
        String dateList = StringUtils.join(Dates, ",");
        String turnoverList = StringUtils.join(turnovers, ",");
        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverList)
                .build();

    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            Map map = new HashMap<>();
            map.put("end", begin);
            Integer totalUser = userMapper.countByMap(map);
           /* if (totalUser==null){
                totalUser=0;
            }*/
            map.put("begin", begin);
            Integer newUser = userMapper.countByMap(map);
            /*if (newUser==null){
                newUser=0;
            }*/
            newUserList.add(newUser);
            totalUserList.add(totalUser);
            begin = begin.plusDays(1);
        }
        return UserReportVO.builder()
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin ,LocalDate end) {
        List<GoodsSalesDTO> goodsSalesTop10 = orderMapper.getGoodsSalesTop10(begin, end);
        StringJoiner nameList=new StringJoiner(",");
        StringJoiner numberList=new StringJoiner(",");
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesTop10) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber().toString());
        }
        return SalesTop10ReportVO.builder()
                .nameList(nameList.toString())
                .numberList(numberList.toString())
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //存放日期的集合
        List<LocalDate> dateList = new ArrayList<>();
        //存放订单数量的集合
        List<Integer> orderCountList = new ArrayList<>();
        //存放完成订单数量的集合
        List<Integer> validOrderCountList = new ArrayList<>();
        //总订单数量
        Integer totalOrderCount=0;
        //总完成订单数量
        Integer totalValidOrderCount=0;
        //在循环中依次获得所需数据，并封装
        while (!begin.isAfter(end)){
            Map map=new HashMap();
            map.put("date", begin);
            Integer orderCount = orderMapper.countByMap(map);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            totalOrderCount=totalOrderCount+orderCount;
            totalValidOrderCount=totalValidOrderCount+validOrderCount;
            dateList.add(begin);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
            begin=begin.plusDays(1);
        }
        double orderCompletionRate=0.0;
        if (totalOrderCount!=0){
            orderCompletionRate=totalValidOrderCount.doubleValue()/totalOrderCount;
        }
        return OrderReportVO.builder()
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .build();
    }
}
