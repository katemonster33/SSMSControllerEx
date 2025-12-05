package ssms.controller.reflection;

import com.fs.starfarer.api.Global;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ConstructorReflector {
    Object ctor;
    static ClassReflector ctorType;
    static MethodReflector newInstanceMethod;
    static MethodReflector getParameterTypes;

    static {
        try {
            ctorType = new ClassReflector(Class.forName("java.lang.reflect.Constructor", false, Class.class.getClassLoader()));

            newInstanceMethod = ctorType.getDeclaredMethod("newInstance", Object[].class);

            getParameterTypes = ctorType.getDeclaredMethod("getParameterTypes");
        } catch(ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    public ConstructorReflector(Object ctor) {
        this.ctor = ctor;
    }

    public Object newInstance(Object ... args) {
        try {
            return newInstanceMethod.invoke(ctor, new Object[]{ args });
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't invoke constructor!", ex);
        }
        return null;
    }

    public Class<?>[] getParameterTypes() {
        return (Class<?>[]) getParameterTypes.invoke(ctor);
    }
}
