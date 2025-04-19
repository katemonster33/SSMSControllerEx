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

    MethodHandle dismiss;
    Object isBeingDismissed;
    MethodHandle actionPerformed;
    Object dialogObject;
    private MessageBoxReflector(Object dialogObject) throws Throwable {
        this.dialogObject = dialogObject;
        var lookup = MethodHandles.lookup();
        dismiss = lookup.findVirtual(dialogObject.getClass(), "dismiss", MethodType.methodType(void.class, int.class));
        actionPerformed = lookup.findVirtual(dialogObject.getClass(), "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));
        isBeingDismissed = ClassReflector.GetInstance().getDeclaredMethod(dialogObject.getClass().getSuperclass().getSuperclass(), "isBeingDismissed");
    }

    static boolean TryGetMessageBoxType(Object obj)
    {
        var lookup = MethodHandles.lookup();
        MethodHandle doNotShowAgain;
        Object showIfNeeded;
        try {
            doNotShowAgain = lookup.findVirtual(obj.getClass(), "doNotShowAgain", MethodType.methodType(boolean.class));
            showIfNeeded = ClassReflector.GetInstance().findDeclaredMethod(obj.getClass(), "showIfNeeded");
        } catch (Throwable ex) {
            Global.getLogger(MessageBoxReflector.class).fatal("Given object is not a dialog object!", ex);
            return false;
        }
        if (doNotShowAgain == null || showIfNeeded == null) {
            return false;
        }
        // we likely found our dialog class, move forward
        messageBoxClass = obj.getClass();
        return true;
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

    public static MessageBoxReflector TryGet(Object obj) {
        if(messageBoxClass == null) {
            if(!TryGetMessageBoxType(obj)) {
                return null;
            }
        }
        // make sure the type of obj is a subclass of our dialog type
        if (obj.getClass() == messageBoxClass) {
            try {
                return new MessageBoxReflector(obj);
            } catch(Throwable ex) {
                Global.getLogger(MessageBoxReflector.class).fatal("Error while attempting to create DialogReflector from existing object!", ex);
            }
        }
        return null;
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
