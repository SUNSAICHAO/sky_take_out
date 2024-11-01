package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);
    void saveWithDish(SetmealDTO setmealDTO);

    void startOrStop(Integer status,long id);

    void updateWithDish(SetmealDTO setmealDTO);

    void deleteBatchByIds(List<Long> ids);
    SetmealVO getById(long id);
}
