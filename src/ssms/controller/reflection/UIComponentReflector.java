package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.campaign.save.LoadGameDialog;

public class UIComponentReflector {
    UIComponentAPI uiComponentAPI;
    static Class<?> uiComponentCls;
    static MethodReflector getParentHandle;
    static MethodReflector getFaderMethod;
    public UIComponentReflector(UIComponentAPI uiComponentAPI) {
        this.uiComponentAPI = uiComponentAPI;
    }

    static {
        uiComponentCls = LoadGameDialog.class.getSuperclass().getSuperclass().getSuperclass().getSuperclass();
        var uiCompReflector = new ClassReflector(uiComponentCls);

        getFaderMethod = uiCompReflector.getDeclaredMethod("getFader");
        getParentHandle = uiCompReflector.getDeclaredMethod("getParent");
    }

    public static Class<?> getUIComponentClass() {
        return uiComponentCls;
    }

    public Fader getFader() {
        return (Fader) getFaderMethod.invoke(uiComponentAPI);
    }

    public UIPanelAPI getParent() {
        return (UIPanelAPI) getParentHandle.invoke(uiComponentAPI);
    }

    public static boolean isComponent(Class<?> cls) {
        return uiComponentCls.isAssignableFrom(cls);
    }
}
