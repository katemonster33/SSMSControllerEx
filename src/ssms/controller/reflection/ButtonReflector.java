package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;

public final class ButtonReflector extends UIComponentReflector {
    static Class<?> buttonType;
    static Object getButtonCheckboxRenderer;
    public static void init(Class<? extends ButtonAPI> buttonType) {
        ButtonReflector.buttonType = buttonType;


    }

    public static boolean isCheckbox(ButtonAPI btn) {
        if(getButtonCheckboxRenderer == null) {
            try {
                getButtonCheckboxRenderer = ClassReflector.GetInstance().findDeclaredMethod(btn.getClass(), "getRendererCheckbox");
            } catch(Throwable ex) {
                Global.getLogger(ButtonReflector.class).warn("Couldn't infer checkbox rendering method from button class!", ex);
            }
        }
        boolean isCheckbox = false;
        try {
            isCheckbox = MethodReflector.GetInstance().invoke(getButtonCheckboxRenderer, btn) != null;
        } catch(Throwable ex) {
            Global.getLogger(ButtonReflector.class).warn("Couldn't tell if button is a check box! :(", ex);
        }
        return isCheckbox;
    }

    public static Class<?> getButtonType() {
        return buttonType;
    }
}
