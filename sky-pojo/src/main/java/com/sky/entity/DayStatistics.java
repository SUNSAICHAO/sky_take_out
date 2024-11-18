package com.sky.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DayStatistics {
    private Double statistics;
    private LocalDate date;
}
