package com.gupaoedu.mvcframework.annotation;

import java.lang.annotation.*;

// 说明了Annotation所修饰的对象范围
// Annotation可被用于 packages、types（类、接口、枚举、Annotation类型）、
// 类型成员（方法、构造方法、成员变量、枚举值）、方法参数和本地变量（如循环变量、catch参数）
@Target({ElementType.FIELD})

// 修饰注解，是注解的注解，称为元注解
// 按生命周期来划分可分为3类：
// 1、RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
// 2、RetentionPolicy.CLASS：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
// 3、RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
@Retention(RetentionPolicy.RUNTIME)

//该注释主要用来生成帮助文档
@Documented 
public @interface GPAutowired {
    String value() default "";
}
