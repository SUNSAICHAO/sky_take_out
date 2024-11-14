package com.sky.mapper;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void update(ShoppingCart shoppingCart);

    @Insert("insert into shopping_cart (name, image, user_id, setmeal_id,dish_id, dish_flavor, amount, create_time)" +
            " VALUES(#{name},#{image},#{userId},#{setmealId},#{dishId},#{dishFlavor},#{amount},#{createTime}) ")
    void add(ShoppingCart shoppingCart);

    void delete(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(long userId);
}
