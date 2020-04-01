package cn.mythoi.processor;

import cn.mythoi.annotation.EnableLuaRoute;
import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * @Author mythoi
 * @Date 2020/3/29 1:39 下午
 * @Description lua文件路由生成
 * @Version V1.0
 **/
@SupportedAnnotationTypes("cn.mythoi.annotation.EnableLuaRoute")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LuaDispatchGenProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer mFiler;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        mFiler = processingEnv.getFiler();
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> classElementSet = roundEnv.getElementsAnnotatedWith(EnableLuaRoute.class);
        for (Element classElement : classElementSet) {
            EnableLuaRoute annotation = classElement.getAnnotation(EnableLuaRoute.class);
            String value = annotation.value().replace("\\\\","/").trim();
            if (!value.endsWith("/")&&!value.equals(""))
                value+="/";
            generateLuaDispatchController(value);
            break;
        }
        return true;
    }

    private void generateLuaDispatchController(String path){

        AnnotationSpec annotationSpecRequestMapping = AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation","RequestMapping"))
                .addMember("value","$S","**").build();

        AnnotationSpec annotationSpecResponseBody = AnnotationSpec.builder(ClassName.get("org.springframework.web.bind.annotation","ResponseBody")).build();

        FieldSpec fieldSpecApplicationContext = FieldSpec.builder(ClassName.get(ApplicationContext.class),"applicationContext",Modifier.PRIVATE)
                .addAnnotation(Autowired.class).build();

        MethodSpec methodBuilder = MethodSpec.methodBuilder("luaDispatch")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpecRequestMapping)
                .addAnnotation(annotationSpecResponseBody)
                .returns(Object.class)
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletRequest"), "httpServletRequest")
                .addParameter(ClassName.get("javax.servlet.http", "HttpServletResponse"), "httpServletResponse")
                .addStatement("String luaFile = httpServletRequest.getRequestURI().substring(1)")
                .addStatement("$T globals = $T.standardGlobals()",ClassName.get("org.luaj.vm2","Globals"),ClassName.get("org.luaj.vm2.lib.jse","JsePlatform"))
                .addStatement("$T loadfile = globals.loadfile($S+luaFile)",ClassName.get("org.luaj.vm2","LuaValue"),path)
                .addStatement("loadfile.jcall(applicationContext)")
                .addStatement("$T main = globals.get($S)",ClassName.get("org.luaj.vm2","LuaValue"),"main")
                .addStatement("Object call1 = main.jcall(httpServletRequest, httpServletResponse)")
                .addStatement("return call1")
                .build();

        TypeSpec luaDispactchController = TypeSpec.classBuilder("LuaDispactchController")
                .addModifiers(Modifier.PUBLIC)
                .addField(fieldSpecApplicationContext)
                .addAnnotation(Controller.class)
                .addMethod(methodBuilder)
                .build();

        JavaFile javaFile = JavaFile.builder("cn.mythoi.generate.component", luaDispactchController)
                .build();
        printMsg(javaFile.toString());
        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void printMsg(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
