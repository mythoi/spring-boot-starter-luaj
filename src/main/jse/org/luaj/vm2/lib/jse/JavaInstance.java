/*******************************************************************************
 * Copyright (c) 2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2.lib.jse;

import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LuaValue that represents a Java instance.
 * <p>
 * Will respond to get() and set() by returning field values or methods.
 * <p>
 * This class is not used directly.
 * It is returned by calls to {@link CoerceJavaToLua#coerce(Object)}
 * when a subclass of Object is supplied.
 *
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaInstance extends LuaUserdata {

    JavaClass jclass;
    private final static int TYPE_GETFILED = 1;
    private final static int TYPE_METHOD = 2;
    private final static int TYPE_CLASS = 3;
    private final static int TYPE_GETTER = 4;
    private final static int TYPE_GETVALUE = 5;

    private final static int TYPE_SETFIELD = 6;
    private final static int TYPE_SETTER = 7;
    private final static int TYPE_SETVALUE = 8;
    private final static int TYPE_SETLISTENER = 9;
    private static HashMap<JavaInstance,HashMap<LuaValue,LuaValue>> values=new HashMap<>();
    private HashMap<LuaValue, LuaValue> vs;
    static final LuaValue CLASS = valueOf("class");

    JavaInstance(Object instance) {
        super(instance);
    }

    @Override
    public LuaValue call(LuaValue arg) {
        if (arg.istable()) {
            LuaValue key = LuaValue.NIL;
            Varargs next;
            while (!(next = arg.next(key)).isnil(1)) {
                key = next.arg1();
                set(key, next.arg(2));
            }
            return this;
        }
        return super.call(arg);
    }

    @Override
    public Varargs invoke(Varargs args) {
        if(args.narg()==1) {
            LuaValue arg = args.arg1();
            if (arg.istable()) {
                LuaValue key = LuaValue.NIL;
                Varargs next;
                while (!(next = arg.next(key)).isnil(1)) {
                    key = next.arg1();
                    set(key, next.arg(2));
                }
                return this;
            }
        }
        return super.invoke(args);
    }

    @Override
    public Varargs next(LuaValue index) {
        if(m_instance instanceof Map){
            Map map= (Map) m_instance;
            Set sets = map.keySet();
            Object key = CoerceLuaToJava.coerce(index, Object.class);
            for (Object set : sets) {
                if(key==null||key.equals(set)){
                      return LuaValue.varargsOf(new LuaValue[]{CoerceJavaToLua.coerce(set),CoerceJavaToLua.coerce(map.get(set))});
                }
            }
        } else if(m_instance instanceof List){
            List list= (List) m_instance;
            int idx = index.isnil() ? 0 : index.toint()+1;
            if (idx>=list.size())
                return LuaValue.NIL;
            return LuaValue.varargsOf(new LuaValue[]{CoerceJavaToLua.coerce(idx),CoerceJavaToLua.coerce(list.get(idx))});
        }
        return super.next(index);
    }

    public LuaValue get(LuaValue key) {
        if (jclass == null)
            jclass = JavaClass.forClass(m_instance.getClass());
        LuaValue val = jclass.finalValueCache.get(key);
        if (val != null)
            return val;
        int type = 0;
        Integer type2 = jclass.typeCache.get(key);
        if (type2 != null)
            type = type2;

        if (type == 0 || type == TYPE_GETFILED) {
            Field f = jclass.getField(key);
            if (f != null) {
                if (type == 0)
                    jclass.typeCache.put(key, TYPE_GETFILED);
                try {
                    LuaValue ret = CoerceJavaToLua.coerce(f.get(m_instance));
                    if (Modifier.isFinal(f.getModifiers())) {
                        jclass.finalValueCache.put(key, ret);
                    }
                    return ret;
                } catch (Exception e) {
                    throw new LuaError(e);
                }
            }
        }

        if (type == 0 || type == TYPE_METHOD) {
            LuaValue m = jclass.getMethod(key);
            if (m != null) {
                if (type == 0)
                    jclass.typeCache.put(key, TYPE_METHOD);
                if(Lua.LUA_JAVA_OO)
                m.setuservalue(this);
                return m;
            }
        }
        if (type == 0 || type == TYPE_CLASS) {
            if(m_instance instanceof Class){
                Class c = jclass.getInnerClass(key);
                if (c != null) {
                    if (type == 0)
                        jclass.typeCache.put(key, TYPE_CLASS);
                    JavaClass ret = JavaClass.forClass(c);
                    if (Modifier.isStatic(c.getModifiers())) {
                        jclass.finalValueCache.put(key, ret);
                    }
                    return ret;
                }
            }
        }
        if (type == 0 || type == TYPE_GETTER) {
            LuaValue m = jclass.getterCache.get(key);
            if (m == null) {
                String k = key.tojstring();
                if(k.equals("class"))
                    return CoerceJavaToLua.coerce(m_instance.getClass());
                if (Character.isLowerCase(k.charAt(0)))
                    k = Character.toUpperCase(k.charAt(0)) + k.substring(1);
                m = jclass.getMethod(CoerceJavaToLua.coerce("get" + k));
                if (m == null)
                    m = jclass.getMethod(CoerceJavaToLua.coerce("is" + k));
            }
            if (m != null) {
                if (type == 0) {
                    jclass.getterCache.put(key, m);
                    jclass.typeCache.put(key, TYPE_GETTER);
                }
                if(Lua.LUA_JAVA_OO)
                    m.setuservalue(this);
                return m.call();
            }
        }

        if (type == 0 || type == TYPE_GETVALUE) {
            if (m_instance instanceof Map) {
                Map map = (Map) m_instance;
                if (type == 0)
                    jclass.typeCache.put(key, TYPE_GETVALUE);
                return CoerceJavaToLua.coerce(map.get(CoerceLuaToJava.coerce(key, Object.class)));
            }
            if (m_instance instanceof List) {
                if (type == 0)
                    jclass.typeCache.put(key, TYPE_GETVALUE);
                List list = (List) m_instance;
                return CoerceJavaToLua.coerce(list.get(key.checkint()));
            }
        }

        if(vs==null){
            if(values.containsKey(this)){
                vs = values.get(this);
                if(vs.containsKey(key))
                    return vs.get(key);
            }
        } else if(vs.containsKey(key)) {
            return vs.get(key);
        }

        if(values.containsKey(jclass)){
            HashMap<LuaValue, LuaValue> cs = values.get(jclass);
            if(cs.containsKey(key))
                return cs.get(key);
        }

        if(key.eq_b(CLASS)){
            jclass.finalValueCache.put(key, jclass);
            return jclass;
        }

        return super.get(key);
    }

    public void set(LuaValue key, LuaValue value) {
        if (jclass == null)
            jclass = JavaClass.forClass(m_instance.getClass());
        int type = 0;
        Integer type2 = jclass.setTypeCache.get(key);
        if (type2 != null)
            type = type2;
        if (type == 0 || type == TYPE_SETFIELD) {
            Field f = jclass.getField(key);
            if (f != null) {
                if (type == 0)
                    jclass.setTypeCache.put(key, TYPE_SETFIELD);
                try {
                    f.set(m_instance, CoerceLuaToJava.coerce(value, f.getType()));
                    return;
                } catch (Exception e) {
                    throw new LuaError(e);
                }
            }
        }
        if (type == 0 || type == TYPE_SETTER) {
            LuaValue m = jclass.setterCache.get(key);
            if (m == null) {
                String k = key.tojstring();
                if (Character.isLowerCase(k.charAt(0)))
                    k = Character.toUpperCase(k.charAt(0)) + k.substring(1);
                m = jclass.getMethod(CoerceJavaToLua.coerce("set" + k));
            }
            if (m != null) {
                if (type == 0) {
                    jclass.setterCache.put(key, m);
                    jclass.setTypeCache.put(key, TYPE_SETTER);
                }
                if(Lua.LUA_JAVA_OO)
                    m.setuservalue(this);
                m.call(value);
                return;
            }
        }
        if (type == 0 || type == TYPE_SETLISTENER) {
            String k = key.tojstring();
            if (k.length() > 2 && k.substring(0, 2).equals("on") && value.isfunction()) {
                if (javaSetListener(k, value)) {
                    if (type == 0)
                        jclass.setTypeCache.put(key, TYPE_SETLISTENER);
                    return;
                }
            }
        }

        if (type == 0 || type == TYPE_SETVALUE) {
            if (m_instance instanceof Map) {
                Map map = (Map) m_instance;
                if (type == 0)
                    jclass.setTypeCache.put(key, TYPE_SETVALUE);
                CoerceJavaToLua.coerce(map.put(CoerceLuaToJava.coerce(key,Object.class), CoerceLuaToJava.coerce(value, Object.class)));
                return;
            }
            if (m_instance instanceof List) {
                if (type == 0)
                    jclass.setTypeCache.put(key, TYPE_SETVALUE);
                List list = (List) m_instance;
                CoerceJavaToLua.coerce(list.set(key.checkint(), CoerceLuaToJava.coerce(value, Object.class)));
                return;
            }
        }

        if(vs==null){
            if(values.containsKey(this)){
                vs = values.get(this);
            } else {
                vs=new HashMap<>();
                values.put(this,vs);
            }
        }
        vs.put(key,value);
        //super.set(key, value);
    }

    private boolean javaSetListener(String k, LuaValue v) {
        String name = "setOn" + k.substring(2) + "Listener";
        JavaMethod m = (JavaMethod) jclass.getMethod(CoerceJavaToLua.coerce(name));
        if (m != null) {
            LuaTable t = new LuaTable();
            t.set(k, v);
            Class<?>[] pt = m.method.getParameterTypes();
            if(Lua.LUA_JAVA_OO)
                m.setuservalue(this);
            m.call(LuajavaLib.createProxy(pt[0], t));
            return true;
        }
        return false;
    }

    @Override
    public LuaValue len() {
        if (m_instance instanceof Map) {
            Map map = (Map) m_instance;
            return CoerceJavaToLua.coerce(map.size());
        }

        if (m_instance instanceof List) {
            List list = (List) m_instance;
            return CoerceJavaToLua.coerce(list.size());
        }
        return super.len();
    }
}
