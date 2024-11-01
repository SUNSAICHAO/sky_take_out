package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*根据菜品id集合批量查找套餐中的菜品*/
    List<SetmealDish>getBatchByDishId(List<Long>list);

    void insertBatch(List<SetmealDish> setmealDishes);
    @Delete("delete from setmeal_dish where setmeal_id =#{id}")
    void deleteBySetmealId(long id);
    void deleteBatchBySetmealId(List<Long>setmealIds);
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(long id);
}
