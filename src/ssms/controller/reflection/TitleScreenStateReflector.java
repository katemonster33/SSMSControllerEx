package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

public class TitleScreenStateReflector {
    Object getScreenPanel = null;
    Object cs;
    static TitleScreenStateReflector instance;
    private TitleScreenStateReflector() {
        cs = AppDriver.getInstance().getState(TitleScreenState.STATE_ID);

        try {
            getScreenPanel = ClassReflector.GetInstance().findDeclaredMethod(TitleScreenState.class, "getScreenPanel");
        } catch(Throwable ex) {
            Global.getLogger(getClass()).fatal("Couldn't reflect into TitleScreenStateReflector!");
        }
    }

    public static TitleScreenStateReflector GetInstance() {
        if(instance == null) {
            instance = new TitleScreenStateReflector();
        }
        return instance;
    }

    public UIPanelAPI getScreenPanel() {
        try {
            return (UIPanelAPI) MethodReflector.GetInstance().invoke(getScreenPanel, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't call TitleScreenState.getScreenPanel!", ex);
            return null;
        }
    }
}
