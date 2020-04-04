package cn.mythoi.annotation;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface EnableLuaRoute {
}
