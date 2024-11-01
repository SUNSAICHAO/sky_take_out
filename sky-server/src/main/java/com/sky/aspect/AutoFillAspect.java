package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Log4j2
@Aspect
@Component
public class AutoFillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段自动填充");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getDeclaredAnnotation(AutoFill.class);
        OperationType value = autoFill.value();
        System.out.println(value);
        Object[] objs = joinPoint.getArgs();
        Object obj = objs[0];
        LocalDateTime localDateTime = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        try {
            Field createTime = obj.getClass().getDeclaredField("createTime");
            Field updateTime = obj.getClass().getDeclaredField("updateTime");
            Field createUser = obj.getClass().getDeclaredField("createUser");
            Field updateUser = obj.getClass().getDeclaredField("updateUser");
            createTime.setAccessible(true);
            updateTime.setAccessible(true);
            createUser.setAccessible(true);
            updateUser.setAccessible(true);
            if (value == OperationType.INSERT) {
                createTime.set(obj, localDateTime);
                updateTime.set(obj, localDateTime);
                createUser.set(obj, currentId);
                updateUser.set(obj, currentId);
            } else {
                updateTime.set(obj, localDateTime);
                updateUser.set(obj, currentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
