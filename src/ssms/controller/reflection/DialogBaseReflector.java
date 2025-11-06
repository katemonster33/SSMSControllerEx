package ssms.controller.reflection;

import com.fs.starfarer.coreui.refit.ModPickerDialogV3;

public class DialogBaseReflector {
    static Class<?> dialogBaseCls;

    static {
        dialogBaseCls = ModPickerDialogV3.class.getSuperclass();
    }
}
