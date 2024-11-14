package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation(value = "根据分类ID查询菜品")
    public Result getWithFlavorByCategoryId(long categoryId) {
        String key = "dish_" + categoryId;
        List<DishVO> dishVOS = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (dishVOS==null){
            dishVOS = dishService.getWithFlavorByCategoryId(categoryId);
            redisTemplate.opsForValue().set(key, dishVOS);
        }
        return Result.success(dishVOS);
    }
}
