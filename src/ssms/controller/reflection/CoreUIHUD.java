package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;

public class CoreUIHUD extends UIPanelReflector {

    static Class<?> coreUiHudType = null;
    static MethodReflector forceSync = null;

    public CoreUIHUD(UIPanelAPI uiPanelAPI) {
        super(uiPanelAPI);
    }

    public static CoreUIHUD tryGet(UIPanelAPI pnl) {
        if(coreUiHudType != null) {
            if(coreUiHudType.isAssignableFrom(pnl.getClass())) {
                return new CoreUIHUD(pnl);
            }
        } else {
            try {
                forceSync = new ClassReflector(pnl.getClass()).getDeclaredMethod("forceSync");
                if(forceSync != null) {
                    coreUiHudType = pnl.getClass();
                    return new CoreUIHUD(pnl);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
