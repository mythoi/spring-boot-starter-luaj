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

    private String baseRoutePath;

    private String baseRunnerPath;

    private String mapping;

    public String getBaseRoutePath() {
        return baseRoutePath;
    }

    public void setBaseRoutePath(String baseRoutePath) {
        this.baseRoutePath = baseRoutePath;
    }

    public String getBaseRunnerPath() {
        return baseRunnerPath;
    }

    public void setBaseRunnerPath(String baseRunnerPath) {
        this.baseRunnerPath = baseRunnerPath;
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
                "baseRoutePath='" + baseRoutePath + '\'' +
                ", baseRunnerPath='" + baseRunnerPath + '\'' +
                ", mapping='" + mapping + '\'' +
                '}';
    }
}
