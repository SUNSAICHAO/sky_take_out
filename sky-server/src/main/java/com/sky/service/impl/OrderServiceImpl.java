package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    //用户端查询历史订单
    @Override
    public PageResult pageQuery(int pageNum, int pageSize, Integer status) {
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        PageHelper.startPage(pageNum, pageSize);
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = page.getResult();
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = UserGetDetailsById(orders.getId());
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOS);
    }

    //管理端条件查询订单
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        int page = ordersPageQueryDTO.getPage();
        int pageSize = ordersPageQueryDTO.getPageSize();
        PageHelper.startPage(page, pageSize);
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> ordersList = orders.getResult();
        ArrayList<OrderVO> orderVOS = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders1 : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders1, orderVO);
                String orderDishes = getOrderDishes(orders1.getId());
                orderVO.setOrderDishes(orderDishes);
                orderVOS.add(orderVO);
            }
        }
        return new PageResult(orders.getTotal(), orderVOS);
    }

    //用户端查询订单详情
    @Override
    public OrderVO UserGetDetailsById(long id) {
        Orders order = orderMapper.getById(id);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    //管理端查询订单详情
    @Override
    public OrderVO AdminGetDetailsById(long id) {
        OrderVO orderVO = UserGetDetailsById(id);
        String orderDishes = getOrderDishes(id);
        orderVO.setOrderDishes(orderDishes);
        return orderVO;
    }

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //查询当前用户的地址簿数据
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook address = addressBookMapper.getById(addressBookId);
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = userMapper.getById(BaseContext.getCurrentId());
        orders.setUserName(user.getName());
        orders.setPhone(address.getPhone());
        orders.setConsignee(address.getConsignee());
        orders.setAddress(address.getProvinceName() + address.getCityName() + address.getDistrictName() + address.getDetail());
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetail.setId(null);
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        // 清空当前用户的购物车数据
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        //返回前端需要的VO对象
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setId(orders.getId());
        orderSubmitVO.setOrderTime(orders.getOrderTime());
        orderSubmitVO.setOrderNumber(orderSubmitVO.getOrderNumber());
        orderSubmitVO.setOrderAmount(orders.getAmount());
        return orderSubmitVO;
    }

    @Override
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }


    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }


    @Override
    public void reminder(long id) {

    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //当订单用户已付款时需要进行退款
        if (orders.getPayStatus().equals(Orders.PAID)) {
            weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            //修改支付状态为退款
            orders.setPayStatus(Orders.REFUND);
        }
        //修改订单状态为已取消
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void userCancel(long id) throws Exception {
        Orders orders = orderMapper.getById(id);
        //查询是否存在要退款的订单
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查询订单状态是否符合退款要求 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单状态为用户已支付，待商家接单
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额,
            //修改支付状态为退款
            orders.setPayStatus(Orders.REFUND);
        }
        //修改订单状态为已取消
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long id = ordersConfirmDTO.getId();
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Long id = ordersRejectionDTO.getId();
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        } else if (!orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        weChatPayUtil.refund(
                orders.getNumber(), //商户订单号
                orders.getNumber(), //商户退款单号
                new BigDecimal(0.01),//退款金额，单位 元
                new BigDecimal(0.01));//原订单金额,
        //修改支付状态为退款
        orders.setPayStatus(Orders.REFUND);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        //修改订单状态为已取消
        orders.setCancelTime(LocalDateTime.now());
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    @Override
    public void complete(long id) {
        Orders orderDB = orderMapper.getById(id);
        //只有订单存在并且在派送中才能够完成订单
        if (orderDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }else if (!orderDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void delivery(long id) {
        Orders orderDB = orderMapper.getById(id);
        //只有订单存在并且已经接单才能够派送订单
        if (orderDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }else if (!orderDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待派送、派送中的订单数量
        Integer toBeConfirmed = orderMapper.CountStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.CountStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.CountStatus(Orders.DELIVERY_IN_PROGRESS);
        //将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void repetition(long id) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        if (details != null && !details.isEmpty()) {
            details.stream().map(detail -> {
                ShoppingCart cart = new ShoppingCart();
                BeanUtils.copyProperties(detail, cart, "id");
                cart.setUserId(BaseContext.getCurrentId());
                cart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.add(cart);
                return cart;
            });
        }
    }

    //根据订单id获取菜品信息字符串
    private String getOrderDishes(long orderId) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);
        StringJoiner stringJoiner = new StringJoiner("");
        for (OrderDetail detail : details) {
            String str = detail.getName() + "*" + detail.getNumber();
            stringJoiner.add(str);
        }
        return stringJoiner.toString();
    }
}
