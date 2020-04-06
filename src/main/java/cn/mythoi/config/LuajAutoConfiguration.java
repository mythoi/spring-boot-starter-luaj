package cn.mythoi.config;

import cn.mythoi.component.SimpleCallComponent;
import cn.mythoi.properties.LuajProperties;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @Author mythoi
 * @Date 2020/3/25 12:31 下午
 * @Description
 * @Version V1.0
 **/

@Configuration
//扫描编译时生成的组件和该库组件
//@ComponentScan({"cn.mythoi.component"})
//Web应用才生效
@ConditionalOnWebApplication
@EnableConfigurationProperties(LuajProperties.class)
public class LuajAutoConfiguration {

    @Autowired
    private LuajProperties luajProperties;

    @Bean
    @Scope("prototype")
    public Globals createGlobals(){
        return JsePlatform.standardGlobals();
    }

    @Bean
    public SimpleCallComponent simpleCallComponent(){
        return new SimpleCallComponent();
    }
}
