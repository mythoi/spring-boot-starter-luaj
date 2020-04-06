package cn.mythoi.annotation;

import cn.mythoi.component.LuaDispactchController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({LuaDispactchController.class})
public @interface EnableLuaRoute {
}
