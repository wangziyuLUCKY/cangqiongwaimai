package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理逻辑
 */
@Aspect //标识当前类是一个AOP类
@Component //把当前类交给Spring容器去管理
@Slf4j  //方便记录一些日志
public class AutoFillAspect {

    /**
     * 切入点：哪些方法需要被拦截
     *  mapper包下所有的类所有的方法，同时还要满足这个方法上加入了AutoFill注解
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 通知：代码增强的业务逻辑部分
     * 应该用的是前置通知，在执行insert和update方法之前，需要为这几个公共字段赋上值。
     * 如果sql执行完毕后在赋值就没有意义了。
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //1.获取到当前被拦截的方法上的数据库操作类型  需要转化为它的子接口MethodSignature
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = autoFill.value();//获得数据库操作类型

        /*
         * 2.获取到当前被拦截的方法的参数--实体对象
         * 约定：如果你想要实现自动填充的话，一定要保证这个实体对象放在第一个参数位置，
         *      因为我们接下来要获取的话就获取第一个就可以了。
         * */
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){ //当前方法没有参数，后续代码没必要执行了，防止出现空指针
            return;
        }

        /*
         * 取出第一个参数，之前已经做了约定 实体放在第一个参数位置
         * 注意：不要使用Employee来接收，因为这个实体类型是不确定的，现在员工管理是
         *      Employee实体类接收参数，后期在分类功能中使用的是Category实体类接收参数，
         *      菜品mapper中传入的是菜品的实体。所以这个地方使用Object来接收。
         *
         * */
        Object entity = args[0];

        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4.根据当前不同的操作类型，为对应的属性通过反射来赋值   OperationType：之前创建的枚举
        if(operationType == OperationType.INSERT){
            //为4个公共字段赋值
            try {
                //通过实体类的set方法为公共的属性赋值，所以需要先获取对应的set方法对象
                //  参数：方法名,方法的形参类型列表    此方法需要处理异常
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值   方法对象.invoke：调用对应的方法
                // 参数：为哪个对象，具体赋的值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

