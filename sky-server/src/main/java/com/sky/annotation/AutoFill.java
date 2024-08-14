package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
@Target(ElementType.METHOD)  //注解使用位置
@Retention(RetentionPolicy.RUNTIME) //注解声明周期
public @interface AutoFill {
    //注解中指定一个属性：指定当前数据库操作的类型（UPDATE INSERT）
    //   注意这几个公共字段只用在UPDATE INSERT操作时才有必要去设置，
    //   查询和删除不需要设置这几个公共字段。
    OperationType value(); //通过枚举方式，OperationType为创建的枚举类
}

