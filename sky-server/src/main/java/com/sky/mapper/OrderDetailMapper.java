package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface OrderDetailMapper {
    @Select("select * from order_detail where order_id =#{id}")
    public List<OrderDetail> getByOrderId(long id);

    void insertBatch(List<OrderDetail> orderDetails);
}
