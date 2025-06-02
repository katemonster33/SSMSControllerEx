package ssms.controller.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FieldReflector {
    private static FieldReflector instance = null;

    Class<?> fieldClass = null;
    MethodHandle setAccessMethod = null;
    MethodHandle getNameMethod = null;
    MethodHandle getTypeMethod = null;
    MethodHandle getMethod = null;
    MethodHandle setMethod = null;

    private FieldReflector() throws Throwable {
        var lookup = MethodHandles.lookup();
        fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

        setAccessMethod = lookup.findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

        getNameMethod = lookup.findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
        
        getTypeMethod = lookup.findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
        
        setMethod = lookup.findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));
        
        getMethod = lookup.findVirtual(fieldClass, "get", MethodType.methodType(Object.class, Object.class));
    }


    public static FieldReflector GetInstance() throws Throwable
    {
        if(instance == null) instance = new FieldReflector();

        return instance;
    }

    public String GetVariableName(Object field) throws Throwable {
        setAccessMethod.invoke(field, true);

        return getNameMethod.invoke(field).toString();
    }

    public Class<?> GetVariableType(Object field) throws Throwable {
        setAccessMethod.invoke(field, true);

        return (Class<?>) getTypeMethod.invoke(field);
    }

    public void SetVariable(Object field, Object thingToSetTo, Object setValue) throws Throwable {
        setAccessMethod.invoke(field, true);

        setMethod.invoke(field, thingToSetTo, setValue);
    }

    public Object GetVariable(Object field, Object thingToGetFrom) throws Throwable {
        setAccessMethod.invoke(field, true);

        return getMethod.invoke(field, thingToGetFrom);
    }

    public Object GetVariableByName(String fieldName, Object thingToGetFrom) throws Throwable {
        Object field = ClassReflector.GetInstance().getDeclaredField(thingToGetFrom.getClass(), fieldName);

        setAccessMethod.invoke(field, true);

        return getMethod.invoke(field, thingToGetFrom);
    }
}
