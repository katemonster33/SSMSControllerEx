package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIComponentAPI;

public class UIComponentReflector {
    UIComponentAPI uiComponentAPI;
    static Class<? extends UIComponentAPI> uiComponentCls;
    static MethodReflector getFaderMethod;
    public UIComponentReflector(UIComponentAPI uiComponentAPI) {
        this.uiComponentAPI = uiComponentAPI;
    }

    static {
        var testPnl = Global.getSettings().createCustom(1.f, 1.f, null);
        var uiCompReflector = new ClassReflector(testPnl.getClass().getSuperclass().getSuperclass());

        getFaderMethod = uiCompReflector.getDeclaredMethod("getFader");
    }

    public Fader getFader() {
        return (Fader) getFaderMethod.invoke(uiComponentAPI);
    }
}
