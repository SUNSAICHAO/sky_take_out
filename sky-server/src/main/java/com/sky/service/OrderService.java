package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
public interface OrderService {
    PageResult pageQuery(int pageNum,int pageSize,Integer status);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO UserGetDetailsById(long id);
    OrderVO AdminGetDetailsById(long id);

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    void paySuccess(String outTradeNo);

    void repetition(long id);


    void userCancel(long id) throws Exception;
    void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;
    void complete(long id);
    void delivery(long id);

    void remind(long id);
}
