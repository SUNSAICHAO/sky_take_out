package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveDishWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setStatus(0);
        dishMapper.save(dish);
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }
        if (flavors != null && flavors.size() > 0) {
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        Page<Dish> dish = dishMapper.pageQuery(dishPageQueryDTO);
        long total = dish.getTotal();
        List<Dish> result = dish.getResult();
        PageResult pageResult = new PageResult(total, result);
        return pageResult;
    }

    @Override
    public DishVO getById(long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        Long id = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        /*更新菜品表*/
        dishMapper.update(dish);
        /*删除原来的菜品对应的口味*/
        dishFlavorMapper.deleteById(id);
        if (flavors.size()>0) {
            System.out.println(flavors);
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(id);
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public void deleteBatchByIds(List<Long> ids) {
        /*检查菜品是否启用*/
        for (Long id : ids) {
            System.out.println(id);
        }
        List<Dish> dishes = dishMapper.getBatchOnSaleById(ids);
        System.out.println(dishes);
        if (dishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        /*检查所选中的菜品是否被套餐所关联*/
        List<SetmealDish> setmealDishes = setmealDishMapper.getById(ids);
        if (setmealDishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        /*批量删除菜品*//*批量删除口味*/
        for (long id : ids) {
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteById(id);
        }
    }

    @Override
    public void startOrStop(Integer status, long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }
}
