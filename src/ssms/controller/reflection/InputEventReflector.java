package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventClass;
import com.fs.starfarer.api.input.InputEventType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InputEventReflector {
    private static InputEventReflector instance;
    MethodHandle arrCtor;
    MethodHandle ctor;
    MethodHandle setDX;
    MethodHandle setDY;
    MethodHandle setX;
    MethodHandle setY;

    MethodHandle addToList;

    public InputEventReflector(Class<?> listType, Class<?> evtType) throws Throwable{
        var lookup = MethodHandles.lookup();
        arrCtor = lookup.findConstructor(listType, MethodType.methodType(void.class));

        addToList = lookup.findVirtual(listType, "add", MethodType.methodType(boolean.class, Object.class));

        ctor = lookup.findConstructor(evtType, MethodType.methodType(void.class, InputEventClass.class, InputEventType.class, int.class, int.class, int.class, char.class));

        setDX = lookup.findVirtual(evtType, "setDX", MethodType.methodType(void.class, int.class));

        setDY = lookup.findVirtual(evtType, "setDY", MethodType.methodType(void.class, int.class));

        setX = lookup.findVirtual(evtType, "setX", MethodType.methodType(void.class, int.class));

        setY = lookup.findVirtual(evtType, "setY", MethodType.methodType(void.class, int.class));
    }

    public static void initializeFromListType(Class<?> cls) {
        Type arrayListCls = cls.getGenericSuperclass();
        ParameterizedType paramType = (ParameterizedType)arrayListCls;
        var inputEventClass = (Class<?>)paramType.getActualTypeArguments()[0];
        try {
            instance = new InputEventReflector(cls, inputEventClass);
        } catch(Throwable ex) {
            Global.getLogger(InputEventReflector.class).warn("Couldn't derive the class of the input events!", ex);
        }
    }

    public static InputEventReflector GetInstance(){
        return instance;
    }

    public ArrayList<?> createList() {
        try {
            return (ArrayList<?>) arrCtor.invoke();
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't create a new input event array!", ex);
            return null;
        }
    }

    public void addToList(List<?> lst, InputEventAPI evt) {
        try {
            addToList.invoke(lst, evt);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't add event to list!", ex);
        }
    }

    public InputEventAPI createInputEvent(InputEventClass var1, InputEventType var2, int var3, int var4, int var5, char var6) throws Throwable{
        return (InputEventAPI) ctor.invoke(var1, var2, var3, var4, var5, var6);
    }

    public InputEventAPI createMouseMoveEvent(int x, int y) throws Throwable {
        var newEvent = createInputEvent(InputEventClass.MOUSE_EVENT, InputEventType.MOUSE_MOVE, 0, 0, 0, '\0');
        setDX.invoke(newEvent, 0);
        setDY.invoke(newEvent, 0);
        setX.invoke(newEvent, x);
        setY.invoke(newEvent, y);
        return newEvent;
    }
}
