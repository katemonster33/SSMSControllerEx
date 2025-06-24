package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;

public final class ButtonReflector extends UIComponentReflector {
    static Class<?> buttonType;
    static MethodReflector getButtonCheckboxRenderer;
    public static void init(Class<? extends ButtonAPI> buttonType) {
        getButtonCheckboxRenderer = new ClassReflector(buttonType).findDeclaredMethod("getRendererCheckbox");
        ButtonReflector.buttonType = buttonType;
    }
    ButtonAPI button;

    public ButtonReflector(ButtonAPI buttonAPI) {
        super(buttonAPI);
    }

    public boolean isCheckbox() {
        return (boolean) getButtonCheckboxRenderer.invoke(button);
    }

    public static Class<?> getButtonType() {
        return buttonType;
    }
}
