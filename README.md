## springboot整合luaj
### springboot整合luaj实现springboot+lua脚本混合开发web
该项目是springboot的starter。目前包含两个注解，用法如下：
@EnableLuaRoute注解开启lua文件路由，可在properties文件里配置luaj.base-route-path来指定路径，默认使用的是classpath下的lua文件，例如：http://localhsot:8080/index.lua 访问classpath下的index.lua文件

@LuaRunner用于方法上
@LuaRunner(value = "test.lua", type = LuaRunnerConstant.LuaRunnerType.BEFORE,func = "main", params = {"aa"})。注解包含四个参数，value为lua文件名，properties里配置luaj.base-runner-path来指定基本路径，该路径会和value值进行拼接，type为lua文件对java方法的切入类型，func为调用的lua函数，params为java给lua传参的字段。例如

```
LuajtestApplication.java文件：

@SpringBootApplication
@RestController
@EnableLuaRoute
public class LuajtestApplication {

    String aa = "123";

    @Autowired
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(LuajtestApplication.class, args);
    }

    @LuaRunner(value = "test.lua", type = LuaRunnerConstant.LuaRunnerType.BEFORE, func = "main",params = {"aa", "applicationContext"})
    @RequestMapping("luaj/{id}")
    public  String luatest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable("id") String id) throws IOException {
        System.out.println(“luatest”);
	return "hello";
    }
}

test.lua文件：

pritn(_aa,_applicationContext)
function main(httpServletRequest, httpServletResponse, id)
    print(id)
end

```
浏览器访问http://localhost:8080/luaj/456  控制台将依次打印</br>
>>123  org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@1198b989</br>
>>456</br>
>>luatest</br>

## 在自己的项目中如何使用？
只需在你的项目中引入此依赖即可
```
  <dependency>
      <groupId>cn.mythoi</groupId>
      <artifactId>spring-boot-starter-luaj</artifactId>
      <version>1.0.2</version>
  </dependency>
```


<a href="https://github.com/nirenr/luaj">该项目引用的luaj版本</a>，后续将在这个版本的基础上进行扩展

持续更新中...
