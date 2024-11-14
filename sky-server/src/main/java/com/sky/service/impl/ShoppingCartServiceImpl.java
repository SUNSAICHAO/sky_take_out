package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /*查看购物车，需要根据当前用户Id查询*/
    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        /*查询购物车内是否已经存在该商品*/
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        /*查询到的结果不为空，说明已存在该商品，需要修改数量*/
        if (shoppingCarts != null && shoppingCarts.size() > 0) {
            shoppingCart = shoppingCarts.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.update(shoppingCart);
            /*查询到的结果为空，说明购物车内不存在该商品，需要添加*/
        } else {
            /*如果套餐Id不为空，说明添加的是套餐*/
            if (shoppingCartDTO.getSetmealId() != null) {
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setSetmealId(setmeal.getId());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.add(shoppingCart);
                /*如果菜品Id不为空，说明添加的是菜品*/
            } else if (shoppingCartDTO.getDishId() != null) {
                Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
                shoppingCart.setDishId(shoppingCartDTO.getDishId());
                shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.add(shoppingCart);
            }
        }
    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        shoppingCart = shoppingCarts.get(0);
        if (shoppingCart.getNumber() != 1) {
            int number = shoppingCart.getNumber();
            shoppingCart.setNumber(number - 1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.update(shoppingCart);
        } else
            shoppingCartMapper.delete(shoppingCart);
    }

    @Override
    public void clean() {
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }
}
