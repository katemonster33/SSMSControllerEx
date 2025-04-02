package ssms.controller;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

import com.fs.starfarer.combat.CombatState;

public class ReflectionHelper {
    Class<?> fieldClass = null;
    MethodHandle setAccessMethod = null;
    MethodHandle getNameMethod = null;
    MethodHandle getTypeMethod = null;
    MethodHandle setMethod = null;
    MethodHandle getDeclaredField = null;


    static ReflectionHelper instance = null;
    private ReflectionHelper() throws Throwable {
        fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

        setAccessMethod = MethodHandles.lookup().findVirtual(fieldClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

        getNameMethod = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String.class));
        
        getTypeMethod = MethodHandles.lookup().findVirtual(fieldClass, "getType", MethodType.methodType(Class.class));
        
        setMethod = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(void.class, Object.class, Object.class));

        getDeclaredField = MethodHandles.lookup().findVirtual(Class.class, "getDeclaredField", MethodType.methodType(Field.class, String.class));
    }

    public ReflectionHelper GetInstance() throws Throwable {
        if(instance == null) instance = new ReflectionHelper();

        return instance;
    }

    public void HideHud()
    {
        Field f = (Field)getDeclaredField.invoke(CombatState.class);
        Field f = CombatState.class.getDeclaredField("hideHud");
        if ( !f.isAccessible() ) f.setAccessible(true);
        f.set(cs, false);
    }

    public void RunMethod(Class<?> ctype, String methodName)
    {

    }

    //made by looking at ReflectionUtils code, except thats on k*tlin
    public String GetVariableName(Object field) throws Throwable {
        setAccessMethod.invoke(field, true);

        return getNameMethod.invoke(field).toString();
    }

    public Class<?> GetVariableType(Object field) throws Throwable {
        if(fieldClass == null) fieldClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());

        setAccessMethod.invoke(field, true);

        return (Class<?>) getTypeMethod.invoke(field);
    }

    public void SetVariable(Object field, Object thingToSetTo, Object setValue) throws Throwable {
        setAccessMethod.invoke(field, true);

        setMethod.invoke(field, thingToSetTo, setValue);
    }

}