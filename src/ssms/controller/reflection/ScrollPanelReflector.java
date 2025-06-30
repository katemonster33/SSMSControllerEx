package ssms.controller.reflection;

import com.fs.starfarer.api.ui.ScrollPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

public class ScrollPanelReflector {
    static MethodReflector ensureVisible;
    ScrollPanelAPI scrollPanelAPI;

    public ScrollPanelReflector(ScrollPanelAPI scrollPanelAPI) {
        this.scrollPanelAPI = scrollPanelAPI;

        ensureVisible = new ClassReflector(scrollPanelAPI.getClass()).findDeclaredMethod("ensureVisible");
    }

    public ScrollPanelAPI getScrollPanel() {
        return scrollPanelAPI;
    }

    public void ensureVisible(UIComponentAPI component) {
        ensureVisible.invoke(scrollPanelAPI, component);
    }
}
