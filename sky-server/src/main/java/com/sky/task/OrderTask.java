package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    //处理超时订单,每分钟触发一次
    @Scheduled(cron = "0 0 1 * * ?")
    public void ProcessOutTimeOrder() {
        log.info("{}:开始处理超时未支付订单", LocalDateTime.now());
        Integer status = Orders.PENDING_PAYMENT;
        //获取15分钟之前下单并且未支付的订单(超时订单)
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> outTimeOrders = orderMapper.getByStatusAndOrderTime(status, orderTime);
        if (outTimeOrders != null && !outTimeOrders.isEmpty()) {
            //取消超时订单
            for (Orders outTimeOrder : outTimeOrders) {
                outTimeOrder.setStatus(Orders.CANCELLED);
                outTimeOrder.setCancelTime(LocalDateTime.now());
                outTimeOrder.setCancelReason("用户超时未支付");
                orderMapper.update(outTimeOrder);
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void ProcessDeliveryOrder() {
        log.info("{}:开始处理配送中的订单", LocalDateTime.now());
        Integer status=Orders.DELIVERY_IN_PROGRESS;
        //获取前一天并且还在配送中的订单
        LocalDateTime orderTime=LocalDateTime.now().minusMinutes(60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(status, orderTime);
        if (ordersList!=null&&!ordersList.isEmpty()){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
