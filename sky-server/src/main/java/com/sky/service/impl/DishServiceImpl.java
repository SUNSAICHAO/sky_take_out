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

import java.util.ArrayList;
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
    public DishVO getWithFlavorById(long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
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
        dishFlavorMapper.deleteByDishId(id);
        if (flavors.size()>0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(id);
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    @Override
    public void deleteBatchByIds(List<Long> ids) {
        /*检查菜品是否启用*/
        List<Dish> dishes = dishMapper.getBatchOnSaleById(ids);
        if (dishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        System.out.println(ids);
        /*检查所选中的菜品是否被套餐所关联*/
        List<SetmealDish> setmealDishes = setmealDishMapper.getBatchByDishId(ids);
        if (setmealDishes.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        /*批量删除菜品*/
        dishMapper.deleteBatchById(ids);
        /*批量删除口味*/
        dishFlavorMapper.deleteBatchByDishId(ids);
    }

    @Override
    public void startOrStop(Integer status, long id) {
        Dish dish = new Dish();
        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> getByCategoryId(long id) {
        List<Dish> dishes = dishMapper.getByCategoryId(id);
        return dishes;
    }

    @Override
    public List<DishVO> getWithFlavorByCategoryId(long categoryId) {
        List<Dish> dishes = dishMapper.getByCategoryId(categoryId);
        List<DishVO> dishVOS = new ArrayList<>();
        for (Dish dish : dishes) {
            Long Id = dish.getId();
            DishVO dishVO = getWithFlavorById(Id);
            dishVOS.add(dishVO);
        }
        return dishVOS;
    }
}
