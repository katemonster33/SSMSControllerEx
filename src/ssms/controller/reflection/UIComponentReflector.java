package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class UIComponentReflector {
    UIComponentAPI uiComponentAPI;
    static Class<? extends UIComponentAPI> uiComponentCls;
    static MethodReflector getParentHandle;
    static MethodReflector getFaderMethod;
    public UIComponentReflector(UIComponentAPI uiComponentAPI) {
        this.uiComponentAPI = uiComponentAPI;
    }

    public static void initialize(Class<?> uiCompCls) {
        var uiCompReflector = new ClassReflector(uiCompCls);

        getFaderMethod = uiCompReflector.getDeclaredMethod("getFader");
        getParentHandle = uiCompReflector.getDeclaredMethod("getParent");
    }

    public Fader getFader() {
        return (Fader) getFaderMethod.invoke(uiComponentAPI);
    }

    public UIPanelAPI getParent() {
        return (UIPanelAPI) getParentHandle.invoke(uiComponentAPI);
    }
}
