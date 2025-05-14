package ssms.controller.reflection;

import com.fs.starfarer.api.ui.ButtonAPI;

public final class ButtonReflector {
    static Class<?> buttonType;

    public static void init(Class<? extends ButtonAPI> buttonType) {
        ButtonReflector.buttonType = buttonType;
    }

    public static Class<?> getButtonType() {
        return buttonType;
    }
}
