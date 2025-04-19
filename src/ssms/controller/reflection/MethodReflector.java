package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodReflector {
    static MethodReflector instance;
    Class<?> methodClass;
    MethodHandle getParameterTypes;
    MethodHandle getName;
    MethodHandle invoke;
    MethodHandle getReturnType;
    MethodHandle setAccessible;
    private MethodReflector() throws Throwable
    {
        var lookup = MethodHandles.lookup();
        methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());

        getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(Class[].class));
        
        getReturnType = lookup.findVirtual(methodClass, "getReturnType", MethodType.methodType(Class.class));
        
        getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));

        invoke = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object.class, Object[].class));

        setAccessible = lookup.findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));
    }

    public static MethodReflector GetInstance() throws Throwable
    {
        if(instance == null) instance = new MethodReflector();

        return instance;
    }

    public Class<?>[] getParameterTypes(Object method) throws Throwable
    {
        return (Class<?>[])getParameterTypes.invoke(method);
    }

    public Class<?> getReturnType(Object method) throws Throwable
    {
        return (Class<?>)getReturnType.invoke(method);
    }

    public String getName(Object method) throws Throwable
    {
        return (String)getName.invoke(method);
    }

    public Object invoke(Object method, Object obj, Object ... arguments) throws Throwable
    {
        return invoke.invoke(method, obj, arguments);
    }

    public void setAccessible(Object method, boolean val) throws Throwable
    {
        setAccessible.invoke(method, val);
    }
}
