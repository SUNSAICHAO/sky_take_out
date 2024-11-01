package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void saveDishWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    DishVO getById(long id);

    void updateWithFlavor(DishDTO dishDTO);

    void deleteBatchByIds(List<Long> ids);

    void startOrStop(Integer status, long id);

    List<Dish> getByCategoryId(long id);
}
