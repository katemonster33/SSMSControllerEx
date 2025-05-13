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

    public UIPanelAPI getPanel() {
        try {
            return (UIPanelAPI) getPanel.invoke(borderedPanel);
        } catch(Throwable ex){
            Global.getLogger(getClass()).fatal("Couldn't get panel from BorderedPanel!", ex);
            return null;
        }
     }

    public static BorderedPanelReflector TryGet(CoreUIAPI coreUIAPI) {
        try {
            var children = UIPanelReflector.getChildItems((UIPanelAPI) coreUIAPI);
            for(var coreuiChild : children) {
                if(UIPanelAPI.class.isAssignableFrom(coreuiChild.getClass())) {
                    if (borderedPanelCls == null) {
                        try {
                            getPanel = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "getPanel", MethodType.methodType(UIPanelReflector.panelType));

                            setPanel = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "setPanel", MethodType.methodType(void.class, UIPanelReflector.panelType));

                            setBorderInsetLeft = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "setBorderInsetLeft", MethodType.methodType(void.class, float.class));

                            setBorderInsetRight = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "setBorderInsetRight", MethodType.methodType(void.class, float.class));

                            setBorderInsetTop = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "setBorderInsetTop", MethodType.methodType(void.class, float.class));

                            setBorderInsetBottom = MethodHandles.lookup().findVirtual(coreuiChild.getClass(), "setBorderInsetBottom", MethodType.methodType(void.class, float.class));

                            borderedPanelCls = coreuiChild.getClass();

                            return new BorderedPanelReflector((UIPanelAPI) coreuiChild, coreUIAPI);
                        } catch (Throwable ex) {
                            Global.getLogger(BorderedPanelReflector.class).info("Failed to reflect type of bordered panel!", ex);
                        }
                    } else if (borderedPanelCls.isAssignableFrom(coreuiChild.getClass())) {
                        return new BorderedPanelReflector((UIPanelAPI) coreuiChild, coreUIAPI);
                    }
                }
            }
            Global.getLogger(BorderedPanelReflector.class).warn("Did not find any border panel UI in the UI tree underneath CoreUI!");
        } catch (Throwable ex) {
            Global.getLogger(BorderedPanelReflector.class).warn("Failed to reflect trade UI object", ex);
        }
        return null;
    }
}
