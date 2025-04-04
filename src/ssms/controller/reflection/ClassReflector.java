package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;

public class ClassReflector {
    static ClassReflector instance;
    MethodHandle getDeclaredField;
    MethodHandle getDeclaredFields;
    MethodHandle getDeclaredMethod;
    MethodHandle getDeclaredMethods;

    private ClassReflector() throws Throwable
    {
        Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
        Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
        Lookup lookup = MethodHandles.lookup();

        getDeclaredField = lookup.findVirtual(Class.class, "getDeclaredField", MethodType.methodType(fieldClass, String.class));
        
        getDeclaredFields = lookup.findVirtual(Class.class, "getDeclaredFields", MethodType.methodType(fieldClass.arrayType()));

        getDeclaredMethod = lookup.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));

        getDeclaredMethods = lookup.findVirtual(Class.class, "getDeclaredMethods", MethodType.methodType(methodClass.arrayType()));
    }

    public static ClassReflector GetInstance() throws Throwable
    {
        if(instance == null) instance = new ClassReflector();

        return instance;
    }

    public Object getDeclaredField(Class<?> cls, String name) throws Throwable {
        return getDeclaredField.invoke(cls, name);
    }

    public Object[] getDeclaredFields(Class<?> cls) throws Throwable{
        return (Object[])getDeclaredFields.invoke(cls);
    }

    public Object getDeclaredMethod(Class<?> cls, String name, Class<?> ... classes) throws Throwable {
        return getDeclaredMethod.invoke(cls, name, classes);
    }

    public Object[] getDeclaredMethods(Class<?> cls) throws Throwable{
        return (Object[])getDeclaredMethods.invoke(cls);
    }
}
