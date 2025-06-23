package ssms.controller.reflection;

import com.fs.starfarer.api.Global;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FieldReflector {
    static Class<?> fieldClass = null;
    static MethodHandle setAccessMethod = null;
    static MethodHandle getNameMethod = null;
    static MethodHandle getTypeMethod = null;
    static MethodHandle getMethod = null;
    static MethodHandle setMethod = null;

    static {
        try {
            var lookup = MethodHandles.lookup();
            fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

            setAccessMethod = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

            getNameMethod = lookup.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));

            getTypeMethod = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));

            setMethod = lookup.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));

            getMethod = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
        } catch(Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
    Object fieldObj;

    public FieldReflector(Object field) {
        assert field.getClass().isAssignableFrom(fieldClass);

        this.fieldObj = field;
    }

    public String getName() {
        try {
            setAccessMethod.invoke(fieldObj, true);

            return getNameMethod.invoke(fieldObj).toString();
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't call setAccessMethod/getNameMethod!", ex);
        }
        return null;
    }

    public Class<?> getType() {
        try {
            setAccessMethod.invoke(fieldObj, true);

            return (Class<?>) getTypeMethod.invoke(fieldObj);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get variable type!", ex);
        }
        return null;
    }

    public void set(Object thingToSetTo, Object setValue) {
        try {
            setAccessMethod.invoke(fieldObj, true);

            setMethod.invoke(fieldObj, thingToSetTo, setValue);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't set field!", ex);
        }
    }

    public Object get(Object thingToGetFrom) {
        try {
            setAccessMethod.invoke(fieldObj, true);

            return getMethod.invoke(fieldObj, thingToGetFrom);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get field value!");
        }
        return null;
    }

//    public static Object GetVariableByName(String fieldName, Object thingToGetFrom) throws Throwable {
//        Object field = ClassReflector.GetInstance().getDeclaredField(thingToGetFrom.getClass(), fieldName);
//
//        setAccessMethod.invoke(field, true);
//
//        return getMethod.invoke(field, thingToGetFrom);
//    }
}
