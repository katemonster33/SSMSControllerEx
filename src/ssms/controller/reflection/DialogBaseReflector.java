package ssms.controller.reflection;

import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.coreui.refit.ModPickerDialogV3;

public class DialogBaseReflector extends UIPanelReflector {
    static Class<?> dialogBaseCls;
    static MethodReflector isBeingDismissed;
    static FieldReflector absorbOutsideEvents;

    static {
        dialogBaseCls = ModPickerDialogV3.class.getSuperclass();

        var dialogBaseReflector = new ClassReflector(dialogBaseCls);

        isBeingDismissed = dialogBaseReflector.getDeclaredMethod("isBeingDismissed");

        for(var field : dialogBaseReflector.getDeclaredFields()) {
            if(boolean.class.isAssignableFrom(field.getType())) {
                absorbOutsideEvents = field;
            }
        }
    }

    public DialogBaseReflector(UIPanelAPI dialog) {
        super(dialog);
    }

    public static Class<?> getDialogBaseCls() {
        return dialogBaseCls;
    }

    public boolean isBeingDismissed(){
        isBeingDismissed.setAccessible(true);

        return (boolean) isBeingDismissed.invoke(panel);
    }

    public boolean getAbsorbOutsideEvents() {
        return (boolean) absorbOutsideEvents.get(panel);
    }
}
