package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBoxReflector {
    // The class representing the highest-order message dialog type
    static Class<?> messageBoxClass;

    static Object isBeingDismissed;
    static MethodHandle actionPerformed;
    static MethodHandle getOptionMap;
    static Object dialogObject;
    private MessageBoxReflector(Object dialogObject) {
        this.dialogObject = dialogObject;
    }

    public static MessageBoxReflector TryGet(UIPanelAPI msgBoxObject)
    {
        if(messageBoxClass == null) {
            var lookup = MethodHandles.lookup();
            try {
                Class<?> clsTmp = msgBoxObject.getClass();
                try {
                    getOptionMap = lookup.findVirtual(clsTmp, "getOptionMap", MethodType.methodType(Map.class));
                } catch(Throwable ex) {
                    try {
                        clsTmp = clsTmp.getSuperclass();

                        getOptionMap = lookup.findVirtual(clsTmp, "getOptionMap", MethodType.methodType(Map.class));
                    } catch(Throwable ex2) {
                        Global.getLogger(MessageBoxReflector.class).warn("Couldn't reflect MessageBox from class!", ex2);
                        return null;
                    }
                }

                actionPerformed = lookup.findVirtual(clsTmp, "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));

                isBeingDismissed = ClassReflector.GetInstance().getDeclaredMethod(clsTmp.getSuperclass(), "isBeingDismissed");

                messageBoxClass = clsTmp;

                return new MessageBoxReflector(msgBoxObject);
            } catch (Throwable ex) {
                Global.getLogger(MessageBoxReflector.class).fatal("Given object is not a dialog object!", ex);
            }
        } else if(msgBoxObject.getClass().isAssignableFrom(messageBoxClass)) {
            return new MessageBoxReflector(msgBoxObject);
        }
        return null;
    }

    public boolean isBeingDismissed(){
        try {
            MethodReflector.GetInstance().setAccessible(isBeingDismissed, true);

            return (boolean) MethodReflector.GetInstance().invoke(isBeingDismissed, dialogObject);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't tell if dialog is being dismissed!");
        }
        return false;
    }

    public List<ButtonAPI> getDialogButtons() {
        List<ButtonAPI> output = new ArrayList<>();
        try {
            Map<?,?> options = (Map<?,?>) getOptionMap.invoke(dialogObject);
            for(Object obj : options.keySet()) {
                if(ButtonAPI.class.isAssignableFrom(obj.getClass())) {
                    output.add((ButtonAPI) output);
                }
            }
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't fetch buttons of dialog!");
        }
        return output;
    }

    public Object getDialogObject() {
        return dialogObject;
    }

    public void doActionPerformed(Object obj1, Object obj2) {
        try {
            actionPerformed.invoke(dialogObject, obj1, obj2);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke actionPerformed on dialog!", ex);
        }
    }
}
