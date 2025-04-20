package ssms.controller.reflection;

import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.input.InputEventClass;
import com.fs.starfarer.api.input.InputEventType;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class InputEventReflector {
    private static InputEventReflector instance;
    MethodHandle ctor;
    MethodHandle setDX;
    MethodHandle setDY;
    MethodHandle setX;
    MethodHandle setY;
    public List<InputEventAPI> inputsToAddToFrame;
    public InputEventReflector(InputEventAPI evt) throws Throwable{
        ctor = MethodHandles.lookup().findConstructor(evt.getClass(), MethodType.methodType(void.class, InputEventClass.class, InputEventType.class, int.class, int.class, int.class, char.class));
        setDX = MethodHandles.lookup().findVirtual(evt.getClass(), "setDX", MethodType.methodType(void.class, int.class));
        setDY = MethodHandles.lookup().findVirtual(evt.getClass(), "setDY", MethodType.methodType(void.class, int.class));
        setX = MethodHandles.lookup().findVirtual(evt.getClass(), "setX", MethodType.methodType(void.class, int.class));
        setY = MethodHandles.lookup().findVirtual(evt.getClass(), "setY", MethodType.methodType(void.class, int.class));
        inputsToAddToFrame = new ArrayList<>();
        instance = this;
    }

    public static InputEventReflector GetInstance(){
        return instance;
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

    public void addEventToList(InputEventAPI evt) {
        inputsToAddToFrame.add(evt);
    }

    public List<InputEventAPI> getAndClearEvents() {
        List<InputEventAPI> newList = new ArrayList<>(inputsToAddToFrame);
        inputsToAddToFrame.clear();
        return newList;
    }
}
