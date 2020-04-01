package cn.mythoi.annotation;


import cn.mythoi.constant.LuaRunnerConstant;

import java.lang.annotation.*;

@Target(value = {ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface LuaRunner {
     String value();
     int type() default LuaRunnerConstant.LuaRunnerType.BEFORE;
     String func() default LuaRunnerConstant.DEFAULTINVOKFUNC;
     String[] params() default {};
}
