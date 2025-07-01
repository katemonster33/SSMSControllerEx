package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.save.LoadGameDialog;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageBoxReflector extends UIPanelReflector {
    // The class representing the highest-order message dialog type
    static Class<?> messageBoxClass;

    static MethodReflector isBeingDismissed;
    static MethodHandle actionPerformed;
    static MethodReflector getOptionMap;
    static MethodReflector getInnerPanel;

    public static void initialize() {
        try {
            Class<?> clsTmp = LoadGameDialog.class.getSuperclass();
            ClassReflector msgBoxReflector = new ClassReflector(clsTmp);
            getOptionMap = msgBoxReflector.getDeclaredMethod("getOptionMap");

            getInnerPanel = msgBoxReflector.findDeclaredMethod("getInnerPanel");

            actionPerformed = MethodHandles.lookup().findVirtual(clsTmp, "actionPerformed", MethodType.methodType(void.class, Object.class, Object.class));

            isBeingDismissed = new ClassReflector(clsTmp.getSuperclass()).getDeclaredMethod("isBeingDismissed");

            messageBoxClass = clsTmp;
        } catch (Throwable ex) {
            Global.getLogger(MessageBoxReflector.class).fatal("Given object is not a dialog object!", ex);
        }
    }

    public static boolean isMsgBox(UIPanelAPI obj) {
        return messageBoxClass.isAssignableFrom(obj.getClass());
    }

    UIPanelAPI dialogObject;
    public MessageBoxReflector(UIPanelAPI dialogObject) {
        super(dialogObject);
        this.dialogObject = dialogObject;
    }

    public boolean isBeingDismissed(){
        isBeingDismissed.setAccessible(true);

        return (boolean) isBeingDismissed.invoke(dialogObject);
    }

    public List<ButtonAPI> getDialogButtons() {
        return getInnerPanel().getChildButtons();
    }

    public UIPanelReflector getInnerPanel() {
        return new UIPanelReflector((UIPanelAPI) getInnerPanel.invoke(dialogObject));
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
