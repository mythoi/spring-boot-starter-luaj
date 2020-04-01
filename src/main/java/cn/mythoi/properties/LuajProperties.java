package cn.mythoi.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author mythoi
 * @Date 2020/3/25 12:25 下午
 * @Description
 * @Version V1.0
 **/

@Component
@ConfigurationProperties(prefix = "luaj")
public class LuajProperties {

    private String luaFilePath;

    private String mapping;

    public String getLuaFilePath() {
        return luaFilePath;
    }

    public void setLuaFilePath(String luaFilePath) {
        this.luaFilePath = luaFilePath;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    @Override
    public String toString() {
        return "LuajProperties{" +
                "luaFilePath='" + luaFilePath + '\'' +
                ", mapping='" + mapping + '\'' +
                '}';
    }
}
