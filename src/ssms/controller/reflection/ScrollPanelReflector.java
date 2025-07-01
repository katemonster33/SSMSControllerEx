package ssms.controller.reflection;

import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class ScrollPanelReflector extends UIPanelReflector {
    static MethodReflector ensureVisible;
    static MethodReflector clampOffset;
    ScrollPanelAPI scrollPanelAPI;
    static MethodReflector scrollToY;
    static MethodReflector getContentContainer;

    public ScrollPanelReflector(ScrollPanelAPI scrollPanelAPI) {
        super((UIPanelAPI) scrollPanelAPI);
        this.scrollPanelAPI = scrollPanelAPI;

        ClassReflector scrollPanelCls =new ClassReflector(scrollPanelAPI.getClass());
        ensureVisible = scrollPanelCls.findDeclaredMethod("ensureVisible");
        clampOffset = scrollPanelCls.getDeclaredMethod("clampOffset");
        scrollToY = scrollPanelCls.getDeclaredMethod("scrollToY", float.class);
        getContentContainer = scrollPanelCls.getDeclaredMethod("getContentContainer");
    }

    public ScrollPanelAPI getScrollPanel() {
        return scrollPanelAPI;
    }

    public void clampOffset() {
        clampOffset.invoke(scrollPanelAPI);
    }

    public UIPanelAPI getContentContainer() {
        return (UIPanelAPI) getContentContainer.invoke(scrollPanelAPI);
    }

    public void ensureVisible(UIComponentAPI component) {
        ensureVisible.invoke(scrollPanelAPI, component);
    }

    public void scrollToY(float yVal) {
        float var2 = getPanel().getPosition().getHeight();
        float var3 = getContentContainer().getPosition().getHeight();
        if (yVal < 0.0F || var2 > var3) {
            yVal = 0.0F;
        } else if ((yVal + var2) > var3) {
            yVal = var3 - var2;
        }
        getScrollPanel().setYOffset(yVal);
    }
}
