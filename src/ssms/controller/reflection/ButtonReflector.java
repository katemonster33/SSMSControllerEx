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
        this.button = buttonAPI;
    }

    public ButtonAPI getButton() {
        return button;
    }

    public boolean isCheckbox() {
        return getButtonCheckboxRenderer.invoke(button) != null;
    }

    public static Class<?> getButtonType() {
        return buttonType;
    }
}
