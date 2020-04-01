package cn.mythoi.processor;

import cn.mythoi.annotation.LuaRunner;
import cn.mythoi.constant.LuaRunnerConstant;
import cn.mythoi.util.JCTreeUtils;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.FileWriter;
import java.util.Set;

/**
 * @Author mythoi
 * @Date 2020/3/25 5:45 下午
 * @Description LuaRunner注解解析器
 * @Version V1.0
 **/
@SupportedAnnotationTypes("cn.mythoi.annotation.LuaRunner")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LuaRunnerProcessor extends AbstractProcessor {

    private Messager messager;

    private Context context;

    private JavacElements elementUtils;

    private TreeMaker treeMaker;

    private JavacTrees trees;

    private Filer mFiler;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        context = ((JavacProcessingEnvironment) processingEnv).getContext();
        elementUtils = (JavacElements) processingEnv.getElementUtils();
        treeMaker = TreeMaker.instance(context);
        trees = JavacTrees.instance(processingEnv);
        mFiler = processingEnv.getFiler();
        JCTreeUtils.init(messager, context, elementUtils, treeMaker);
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> methodElementSet = roundEnv.getElementsAnnotatedWith(LuaRunner.class);
        methodElementSet.forEach(element -> {
            LuaRunner annotation = element.getAnnotation(LuaRunner.class);
            String annotationValue = annotation.value();
            String annotationFuncName = annotation.func();
            String[] params = annotation.params();
            int annotationType = annotation.type();
            JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) trees.getTree(element);
            if (jcMethodDecl.mods.getFlags().toString().contains("static")) {
                messager.printMessage(Diagnostic.Kind.WARNING, "LuaRunner注解不支持静态类型！");
                return;
            }
            JCTree.JCVariableDecl globalsBlock = JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("org.luaj.vm2.Globals"), "globals", treeMaker.Apply(List.<JCTree.JCExpression>nil(), JCTreeUtils.memberAccess("org.luaj.vm2.lib.jse.JsePlatform.standardGlobals"), List.<JCTree.JCExpression>nil()));
            JCTree.JCVariableDecl chunkBlock1 = JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("org.luaj.vm2.LuaValue"), "chunk1", treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("globals.loadfile"), List.of(treeMaker.Literal(annotationValue))));
            List<JCTree.JCExpression> paramsList = List.nil();
            List<JCTree.JCExpression> paramsTypeList = List.nil();
            for (JCTree.JCVariableDecl param : jcMethodDecl.params) {
                paramsList = paramsList.append(treeMaker.Ident(param.name));
                paramsTypeList = paramsTypeList.append(param.vartype);
            }
            //JCTree.JCExpressionStatement luaValueExecBlock1 = treeMaker.Exec(treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("chunk1.jcall"), List.of(treeMaker.Ident(elementUtils.getName("this")))));
            JCTree.JCExpressionStatement _this = treeMaker.Exec(treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("globals.jset"), List.of(treeMaker.Literal("_this"), treeMaker.Ident(elementUtils.getName("this")))));
            List<JCTree.JCStatement> paramList = List.nil();
            JCTree.JCExpressionStatement luaValueExecBlock1 = treeMaker.Exec(treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("chunk1.call"), List.nil()));
            for (String param : params) {
                JCTree.JCExpressionStatement exec = treeMaker.Exec(treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("globals.jset"), List.of(treeMaker.Literal("_" + param), treeMaker.Ident(elementUtils.getName(param)))));
                paramList = paramList.append(exec);
            }
            JCTree.JCVariableDecl chunkBlock2 = JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("org.luaj.vm2.LuaValue"), "chunk2", treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("globals.get"), List.of(treeMaker.Literal(annotationFuncName))));
            JCTree.JCVariableDecl luaValueExecBlock2 = JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("java.lang.Object"), "jcallReturn", treeMaker.Apply(paramsTypeList, JCTreeUtils.memberAccess("chunk2.jcall"), paramsList));

            switch (annotationType) {
                case LuaRunnerConstant.LuaRunnerType.BEFORE:
                    jcMethodDecl.body = treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2, jcMethodDecl.body)));
                    break;
                case LuaRunnerConstant.LuaRunnerType.AFTER:
                    jcMethodDecl.body = treeMaker.Block(0, List.of(treeMaker.Try(jcMethodDecl.body, List.nil(), treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2))))));
                    break;
                case LuaRunnerConstant.LuaRunnerType.ALL:
                    if (jcMethodDecl.restype.getKind() != JCTree.Kind.PRIMITIVE_TYPE)
                        jcMethodDecl.body = treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2, treeMaker.Return(treeMaker.TypeCast(jcMethodDecl.restype, treeMaker.Ident(elementUtils.getName("jcallReturn")))))));
                    else
                        jcMethodDecl.body = treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2)));
                    break;
                case LuaRunnerConstant.LuaRunnerType.THROW:
                    List<JCTree.JCCatch> catchList;
                    JCTree.JCExpressionStatement _exception = treeMaker.Exec(treeMaker.Apply(List.nil(), JCTreeUtils.memberAccess("globals.jset"), List.of(treeMaker.Literal("_exception"), treeMaker.Ident(elementUtils.getName("exception")))));
                    if (jcMethodDecl.restype.getKind() != JCTree.Kind.PRIMITIVE_TYPE)
                        catchList = List.of(treeMaker.Catch(JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("java.lang.Exception"), "exception",
                                null), treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this,_exception).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2, treeMaker.Return(treeMaker.TypeCast(jcMethodDecl.restype, treeMaker.Ident(elementUtils.getName("jcallReturn")))))))));
                    else
                        catchList = List.of(treeMaker.Catch(JCTreeUtils.makeVarDef(treeMaker.Modifiers(0), JCTreeUtils.memberAccess("java.lang.Exception"), "exception",
                                null), treeMaker.Block(0, List.of(globalsBlock, chunkBlock1, _this,_exception).appendList(paramList).appendList(List.of(luaValueExecBlock1, chunkBlock2, luaValueExecBlock2)))));
                    jcMethodDecl.body = treeMaker.Block(0, List.of(treeMaker.Try(jcMethodDecl.body, catchList, null)));
                    break;
            }
            printMsg(jcMethodDecl.body.toString());
        });
        return true;
    }


    //将内容输出到文件
    private void generateFile(String str) {
        try {
            //这是mac环境下的路径
            File file = new File("/Users/mythoi/code/dbCustomProcFile");
            FileWriter fw = new FileWriter(file);
            fw.append(str);

            fw.flush();
            fw.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            printMsg(e.toString());
        }
    }

    private void printMsg(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
