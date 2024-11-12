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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public List<ShoppingCart> list() {
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list();
        return shoppingCarts;
    }

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        /*查询购物车内是否已经存在该商品*/
        ShoppingCart result = shoppingCartMapper.get(shoppingCartDTO);
        /*查询到的结果不为空，说明已存在该商品，需要修改*/
        if (result != null) {
            /*如果套餐Id不为空，说明添加的是套餐*/
            if (result.getSetmealId() != null) {
                Setmeal setmeal = setmealMapper.getById(result.getSetmealId());
                result.setNumber(result.getNumber() + 1);
                result.setAmount(result.getAmount().add(setmeal.getPrice()));
                result.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.update(result);
                /*如果菜品Id不为空，说明添加的是菜品*/
            } else if (result.getDishId() != null) {
                Dish dish = dishMapper.getById(result.getDishId());
                result.setNumber(result.getNumber() + 1);
                result.setAmount(result.getAmount().add(dish.getPrice()));
                result.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.update(result);
            }
            /*查询到的结果为空，说明购物车内不存在该商品，需要添加*/
        } else {
            ShoppingCart shoppingCart = new ShoppingCart();
            /*如果套餐Id不为空，说明添加的是套餐*/
            if (shoppingCartDTO.getSetmealId() != null) {
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setSetmealId(setmeal.getId());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setNumber(1);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCart.setUserId(BaseContext.getCurrentId());
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
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCartMapper.add(shoppingCart);
            }
        }
    }

    @Override
    public void delete(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = shoppingCartMapper.get(shoppingCartDTO);
        if (shoppingCart.getNumber() != 1) {
            int number = shoppingCart.getNumber();
            BigDecimal bigDecimalAmount = shoppingCart.getAmount();
            /*double amount = Double.parseDouble(bigDecimalAmount.toString());
            double price = amount / number;
            BigDecimal bigDecimalPrice = BigDecimal.valueOf(price);*/
            shoppingCart.setNumber(number - 1);
            shoppingCart.setAmount(bigDecimalAmount.subtract(bigDecimalAmount.divide(BigDecimal.valueOf(number))));
            /*shoppingCart.setAmount(bigDecimalAmount.subtract(bigDecimalPrice));*/
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.update(shoppingCart);
        } else
            shoppingCartMapper.delete(shoppingCartDTO);
    }

    @Override
    public void clean() {
        shoppingCartMapper.clean(BaseContext.getCurrentId());
    }
}
