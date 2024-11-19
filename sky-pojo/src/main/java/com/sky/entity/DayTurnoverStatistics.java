package com.sky.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
//日营业额统计类
public class DayTurnoverStatistics {
    //当天的日期
    private LocalDate date;
    //营业额
    private Double statistics;
}
