package ssms.controller.reflection;

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.refit.ModPickerDialogV3;

public class DialogBaseReflector extends UIPanelReflector {
    static Class<?> dialogBaseCls;
    static MethodReflector isBeingDismissed;

    static {
        dialogBaseCls = ModPickerDialogV3.class.getSuperclass();


        isBeingDismissed = new ClassReflector(dialogBaseCls).getDeclaredMethod("isBeingDismissed");
    }

    public DialogBaseReflector(UIPanelAPI dialog) {
        super(dialog);
    }

    public boolean isBeingDismissed(){
        isBeingDismissed.setAccessible(true);

        return (boolean) isBeingDismissed.invoke(panel);
    }
}
