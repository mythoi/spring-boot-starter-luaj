package cn.mythoi.util;

import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.Messager;

/**
 * @Author mythoi
 * @Date 2020/3/27 9:16 上午
 * @Description
 * @Version V1.0
 **/
public final class JCTreeUtils {

    private static Messager messager;

    private static Context context;

    private static JavacElements elementUtils;

    private static TreeMaker treeMaker;

    public static void init(Messager messager1,Context context1,JavacElements javacElements1,TreeMaker treeMaker1){
        messager = messager1;
        context = context1;
        elementUtils = javacElements1;
        treeMaker = treeMaker1;
    }

    /**
     * 创建多级访问
     * @param components
     * @return
     */
    public static JCTree.JCExpression memberAccess(String components) {
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(elementUtils.getName(componentArray[0]));
        for (int i = 1; i < componentArray.length; i++) {
            expr = treeMaker.Select(expr, elementUtils.getName(componentArray[i]));
        }
        return expr;
    }


    /**
     * 创建变量语句并赋值
     * @param modifiers 变量修饰
     * @param vartype 变量类型
     * @param name 变量名
     * @param init 初始化值
     * @return
     */
    public static JCTree.JCVariableDecl makeVarDef(JCTree.JCModifiers modifiers, JCTree.JCExpression vartype,String name, JCTree.JCExpression init) {
        return treeMaker.VarDef(
                modifiers,
                elementUtils.getName(name), //名字
                vartype, //类型
                init //初始化语句
        );
    }

    /**
     * 变量赋值
     * @param lhs
     * @param rhs
     * @return
     */
    public static JCTree.JCExpressionStatement makeAssignment(JCTree.JCExpression lhs, JCTree.JCExpression rhs) {
        return treeMaker.Exec(
                treeMaker.Assign(
                        lhs,
                        rhs
                )
        );
    }
}
