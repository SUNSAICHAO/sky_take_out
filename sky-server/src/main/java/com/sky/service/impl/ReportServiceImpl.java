package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //根据传入的日期区间统计每天的营业额，缺陷：如果某天没有开业或者没有营业额，就无法统计到当天的营业额
        // 因此只能一天一天统计营业额，如果营业额数据为空，要把当天的营业额设置为0；
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
}
