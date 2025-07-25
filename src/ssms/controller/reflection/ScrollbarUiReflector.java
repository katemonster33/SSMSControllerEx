package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class ScrollbarUiReflector extends UIPanelReflector {

    MethodHandle getScrollbarValue;

    public ScrollbarUiReflector(UIPanelAPI scrollbarObj) throws Throwable {
        super(scrollbarObj);

        getScrollbarValue = MethodHandles.lookup().findVirtual(scrollbarObj.getClass(), "getValue", MethodType.methodType(int.class));
    }

    public UIPanelAPI getPrivateObj() {
        return panel;
    }

    public int getValue() {
        try {
            return (int)getScrollbarValue.invoke(panel);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't get scrollbar's value!", ex);
        }
        return -1;
    }
}
