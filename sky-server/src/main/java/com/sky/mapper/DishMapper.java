package com.sky.mapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DishMapper {
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void save(Dish dish);

    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getById(long id);


    List<Dish> getBatchById(List<Long>ids);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);



    void deleteBatchById(List<Long> ids);

    @Select("select * from dish where category_id = #{id} and status = 1")
    List<Dish> getByCategoryId(long id);

}
