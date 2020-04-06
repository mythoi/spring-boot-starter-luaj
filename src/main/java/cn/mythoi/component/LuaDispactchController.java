package cn.mythoi.component;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@ConditionalOnWebApplication
public class LuaDispactchController {

  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  private SimpleCallComponent simpleCallComponent;

  @RequestMapping(
      value = "/**/*.lua/{func}",
      method = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE,RequestMethod.HEAD,RequestMethod.OPTIONS,RequestMethod.PATCH,RequestMethod.TRACE}
  )
  @ResponseBody
  public Object luaDispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, @PathVariable String func) throws Exception {
    String requestURI = httpServletRequest.getRequestURI();
    String luaFile = requestURI.substring(1,requestURI.lastIndexOf(func)-1);
    Globals globals = JsePlatform.standardGlobals();
    LuaValue loadfile = null;
    try {
      loadfile = globals.loadfile(simpleCallComponent.getBaseRoutePath()+luaFile);
      globals.jset("_applicationContext",applicationContext);
      globals.jset("_this",this);
    }
    catch(Exception e) {
      httpServletRequest.getRequestDispatcher("error/404.html").forward(httpServletRequest,httpServletResponse);
      System.err.println(e);
      return null;
    }
    loadfile.call();
    LuaValue main = globals.get(func);
    if (!main.isfunction()) {
      httpServletRequest.getRequestDispatcher("error/404.html").forward(httpServletRequest,httpServletResponse);
      return null;
    }
    Object call1 = main.jcall(httpServletRequest, httpServletResponse);
    return call1;
  }
}
