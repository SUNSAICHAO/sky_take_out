package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    @AutoFill(OperationType.INSERT)
    void save(Setmeal setmeal);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    List<Setmeal> getBatchOnSaleById(List<Long> ids);

    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(long id);

    void deleteBatchById(List<Long> ids);
    @Select("select * from setmeal where category_id = #{categoryId}")
    List<Setmeal> getByCategoryId(long categoryId);
}
