package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Select;

public interface OrderService {
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
    OrderVO getById(long id);
}
