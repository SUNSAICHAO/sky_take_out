package com.sky.mapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openID}")
    User getByOpenID(String openID);

    void insert(User user);


    @Select("select * from user where id = #{userId}")
    User getById(long userId);

 Integer countByMap(Map map);


}
