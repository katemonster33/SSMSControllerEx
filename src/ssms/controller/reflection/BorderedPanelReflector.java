package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class BorderedPanelReflector {
    UIPanelAPI borderedPanel;
    CoreUIAPI coreUIAPI;
    static MethodHandle getPanel;
    static MethodHandle setPanel;
    static MethodHandle setBorderInsetLeft;
    static MethodHandle setBorderInsetRight;
    static MethodHandle setBorderInsetTop;
    static MethodHandle setBorderInsetBottom;
    static Class<?> borderedPanelCls;

    private BorderedPanelReflector(UIPanelAPI borderedPanel, CoreUIAPI coreUIAPI) {
        this.borderedPanel = borderedPanel;
        this.coreUIAPI = coreUIAPI;
    }

    public CoreUIAPI getCoreUIAPI() {
        return coreUIAPI;
    }

    public UIPanelAPI getBorderedPanel() {
        return borderedPanel;
    }

    public UIPanelAPI getInnerPanel() {
        try {
            return (UIPanelAPI) getPanel.invoke(borderedPanel);
        } catch(Throwable ex){
            Global.getLogger(getClass()).fatal("Couldn't get panel from BorderedPanel!", ex);
            return null;
        }
     }

    public static BorderedPanelReflector TryGet(CoreUIAPI coreUIAPI, UIPanelAPI panelObj) {
        try {
            if (borderedPanelCls == null) {
                try {
                    getPanel = MethodHandles.lookup().findVirtual(panelObj.getClass(), "getPanel", MethodType.methodType(UIPanelReflector.panelType));

                    setPanel = MethodHandles.lookup().findVirtual(panelObj.getClass(), "setPanel", MethodType.methodType(void.class, UIPanelReflector.panelType));

                    setBorderInsetLeft = MethodHandles.lookup().findVirtual(panelObj.getClass(), "setBorderInsetLeft", MethodType.methodType(void.class, float.class));

                    setBorderInsetRight = MethodHandles.lookup().findVirtual(panelObj.getClass(), "setBorderInsetRight", MethodType.methodType(void.class, float.class));

                    setBorderInsetTop = MethodHandles.lookup().findVirtual(panelObj.getClass(), "setBorderInsetTop", MethodType.methodType(void.class, float.class));

                    setBorderInsetBottom = MethodHandles.lookup().findVirtual(panelObj.getClass(), "setBorderInsetBottom", MethodType.methodType(void.class, float.class));

                    borderedPanelCls = panelObj.getClass();

                    return new BorderedPanelReflector((UIPanelAPI) panelObj, coreUIAPI);
                } catch (Throwable ex) {
                    Global.getLogger(BorderedPanelReflector.class).info("Failed to reflect type of bordered panel!", ex);
                }
            } else if (borderedPanelCls.isAssignableFrom(panelObj.getClass())) {
                return new BorderedPanelReflector((UIPanelAPI) panelObj, coreUIAPI);
            }
        } catch (Throwable ex) {
            Global.getLogger(BorderedPanelReflector.class).warn("Failed to reflect trade UI object", ex);
        }
        return null;
    }
}
