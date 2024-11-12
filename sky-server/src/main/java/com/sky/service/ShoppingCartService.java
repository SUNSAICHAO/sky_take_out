package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    public List<ShoppingCart> list ();

    void add(ShoppingCartDTO shoppingCartDTO);

    void delete(ShoppingCartDTO shoppingCartDTO);

    void clean();
}
