package ssms.controller.reflection;

import com.fs.starfarer.api.Global;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodReflector {
    static Class<?> methodClass;
    static MethodHandle getParameterTypes;
    static MethodHandle getName;
    static MethodHandle invoke;
    static MethodHandle getReturnType;
    static MethodHandle setAccessible;
    static
    {
        try {
            var lookup = MethodHandles.lookup();
            methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());

            getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));

            getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));

            getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));

            invoke = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));

            setAccessible = lookup.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    Object methodObj;

    public MethodReflector(Object method) {
        assert method.getClass().isAssignableFrom(methodClass);
        this.methodObj = method;
    }

    public Class<?>[] getParameterTypes()
    {
        try {
            return (Class<?>[]) getParameterTypes.invoke(methodObj);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get parameter types of method!", ex);
        }
        return null;
    }

    public Class<?> getReturnType() {
        try {
            return (Class<?>) getReturnType.invoke(methodObj);
        } catch (Throwable e) {
            Global.getLogger(getClass()).error("Couldn't get return type of method!", e);
        }
        return null;
    }

    public String getName()
    {
        try {
            return (String) getName.invoke(methodObj);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get name of method!", ex);
        }
        return null;
    }

    public Object invoke(Object obj, Object ... arguments)
    {
        try {
            return invoke.invoke(methodObj, obj, arguments);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't invoke method!", ex);
        }
        return null;
    }

    public void setAccessible(boolean val)
    {
        try {
            setAccessible.invoke(methodObj, val);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't set method accessible!", ex);
        }
    }
}
