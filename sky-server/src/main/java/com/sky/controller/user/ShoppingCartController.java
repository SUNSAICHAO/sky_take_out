package com.sky.controller.user;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags = "C端-购物车接口")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    @ApiOperation(value = "查看购物车")
    public Result list() {
        List<ShoppingCart> shoppingCarts = shoppingCartService.list();
        return Result.success(shoppingCarts);
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation(value = "删除购物车中一个商品")
    public Result delete(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清空购物车")
    public Result clean() {
        shoppingCartService.clean();
        return Result.success();
    }
}
