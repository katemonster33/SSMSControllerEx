package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Objects;

import org.apache.log4j.Level;

import com.fs.starfarer.api.Global;

public class ClassReflector {
    static ClassReflector instance;
    static MethodHandle getDeclaredField;
    static MethodHandle getDeclaredFields;
    static MethodHandle getDeclaredMethod;
    static MethodHandle getDeclaredMethods;

    static
    {
        try {
            Class<?> fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
            Class<?> methodClass = Class.forName("java.lang.reflect.Method", false, Class.class.getClassLoader());
            Lookup lookup = MethodHandles.lookup();

            getDeclaredField = lookup.findVirtual(Class.class, "getDeclaredField", MethodType.methodType(fieldClass, String.class));

            getDeclaredFields = lookup.findVirtual(Class.class, "getDeclaredFields", MethodType.methodType(fieldClass.arrayType()));

            getDeclaredMethod = lookup.findVirtual(Class.class, "getDeclaredMethod", MethodType.methodType(methodClass, String.class, Class[].class));

            getDeclaredMethods = lookup.findVirtual(Class.class, "getDeclaredMethods", MethodType.methodType(methodClass.arrayType()));
        } catch(Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
    Class<?> cls;

    public ClassReflector(Class<?> cls) {
        this.cls = cls;
    }

    public Object getDeclaredField(String name) {
        try {
            return getDeclaredField.invoke(cls, name);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get declared fields of class!", ex);
        }
        return null;
    }

    public Object[] getDeclaredFields() {
        try {
            return (Object[]) getDeclaredFields.invoke(cls);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't invoke getDeclaredFields!", ex);
        }
        return null;
    }

    public MethodReflector getDeclaredMethod(String name, Class<?> ... classes) {
         try {
             return new MethodReflector(getDeclaredMethod.invoke(cls, name, classes));
         } catch(Throwable ex) {
             Global.getLogger(getClass()).error("Couldn't invoke getDeclaredMethods!", ex);
         }
         return null;
    }

    public MethodReflector findDeclaredMethod(String name) {
        for(var method : getDeclaredMethods()) {
            if(Objects.equals(method.getName(), name)) {
                return method;
            }
        }
        for(Class<?> superclass = cls.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
            for(var method : getDeclaredMethods(superclass)) {
                if(Objects.equals(method.getName(), name)) {
                    return method;
                }
            }
        }
        Global.getLogger(getClass()).log(Level.WARN, "Unable to find method! cls=" + cls + ", name=" + name);
        return null;
    }

    public MethodReflector[] getDeclaredMethods() {
        MethodReflector[] output = null;
        try {
            Object[] declaredMethod = (Object[]) getDeclaredMethods.invoke(cls);
            if (declaredMethod != null) {
                output = new MethodReflector[declaredMethod.length];
                for (int i = 0; i < declaredMethod.length; i++) {
                    output[i] = new MethodReflector(declaredMethod[i]);
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't invoke getDeclaredMethods!");
        }
        return output;
    }
}
