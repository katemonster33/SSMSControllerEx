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
    private MethodReflector() throws Throwable
    {
        var lookup = MethodHandles.lookup();
        Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
        methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());

        getParameterTypes = lookup.findVirtual(methodClass, "getParameterTypes", MethodType.methodType(fieldClass.arrayType()));
        
        getName = lookup.findVirtual(methodClass, "getName", MethodType.methodType(String.class));

        invoke = lookup.findVirtual(methodClass, "invoke", MethodType.methodType(Object.class, Object[].class));
    }

    public static MethodReflector GetInstance() throws Throwable
    {
        if(instance == null) instance = new MethodReflector();

        return instance;
    }

    public Object[] getParameterTypes(Object method) throws Throwable
    {
        return (Object[])getParameterTypes.invoke(method);
    }

    public String getName(Object method) throws Throwable
    {
        return (String)getName.invoke(method);
    }

    public Object invoke(Object method, Object ... arguments) throws Throwable
    {
        return invoke.invoke(method, arguments);
    }
}
