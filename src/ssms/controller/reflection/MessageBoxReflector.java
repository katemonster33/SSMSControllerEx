package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public class MessageBoxReflector {
    // The class representing the highest-order message dialog type
    static Class<?> messageBoxClass;

    static MethodHandle dismiss;
    static Object isBeingDismissed;
    static MethodHandle actionPerformed;
    static Object dialogObject;
    private MessageBoxReflector(Object dialogObject) {
        this.dialogObject = dialogObject;
    }

    public static MessageBoxReflector TryGet(UIPanelAPI msgBoxObject)
    {
        if(messageBoxClass == null) {
            try {
                var lookup = MethodHandles.lookup();
                dismiss = lookup.findVirtual(msgBoxObject.getClass(), "dismiss", MethodType.methodType(void.class, int.class));

                actionPerformed = lookup.findVirtual(msgBoxObject.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));

                isBeingDismissed = ClassReflector.GetInstance().getDeclaredMethod(msgBoxObject.getClass().getSuperclass().getSuperclass(), "isBeingDismissed");

                messageBoxClass = msgBoxObject.getClass();

                return new MessageBoxReflector(msgBoxObject);
            } catch (Throwable ex) {
                Global.getLogger(MessageBoxReflector.class).fatal("Given object is not a dialog object!", ex);
            }
        } else if(messageBoxClass.isAssignableFrom(msgBoxObject.getClass())) {
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
            var getInnerPanel = ClassReflector.GetInstance().findDeclaredMethod(dialogObject.getClass(), "getInnerPanel");
            var innerPanel = MethodReflector.GetInstance().invoke(getInnerPanel, dialogObject);
            output.addAll(UIPanelReflector.getChildButtons((UIPanelAPI) innerPanel));
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

    public void doDismiss() {
        try {
            dismiss.invoke(dialogObject, 1);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Could not invoke dismiss on dialog!", ex);
        }
    }
}
