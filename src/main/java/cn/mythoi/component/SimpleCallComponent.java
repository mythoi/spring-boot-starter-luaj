package cn.mythoi.component;

import cn.mythoi.properties.LuajProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author mythoi
 * @Date 2020/4/3 3:48 下午
 * @Description 简单调用类，用于生成代码时简化处理逻辑
 * @Version V1.0
 **/
@Component
public class SimpleCallComponent {

    @Autowired
    private LuajProperties luajProperties;


    public String getBaseRoutePath(){
        String luaRoutePath = luajProperties.getBaseRoutePath();
        luaRoutePath = (luaRoutePath==null?"":luaRoutePath).replace("\\","/").trim();
        if (!luaRoutePath.endsWith("/")&&!"".equals(luaRoutePath)) {
            luaRoutePath += "/";
        }
        return luaRoutePath;
    }

    public String getBaseRunnerPath(){
        String luaRunnerath = luajProperties.getBaseRunnerPath();
        luaRunnerath = (luaRunnerath==null?"":luaRunnerath).replace("\\","/").trim();
        if (!luaRunnerath.endsWith("/")&&!"".equals(luaRunnerath)) {
            luaRunnerath += "/";
        }
        return luaRunnerath;
    }
}
