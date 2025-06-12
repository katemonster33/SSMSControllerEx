package ssms.controller.reflection;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

import java.util.ArrayList;
import java.util.List;

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

    public List<ButtonAPI> getMainMenuButtons() {
        List<ButtonAPI> output = new ArrayList<>();

        try {
            var widgets = UIPanelReflector.getChildItems(getScreenPanel());
            var mainMenu = (UIPanelAPI) widgets.get(0);
            var getMainMenu = ClassReflector.GetInstance().findDeclaredMethod(mainMenu.getClass(), "getMainMenu");

            var mainMenuPanel = (UIPanelAPI) MethodReflector.GetInstance().invoke(getMainMenu, mainMenu);

            var mainMenuWidgets = UIPanelReflector.getChildItems(mainMenuPanel);

            if (!mainMenuWidgets.isEmpty()) {

                output.addAll(UIPanelReflector.getChildButtons((UIPanelAPI) mainMenuWidgets.get(0)));

            }

        } catch (Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't fetch buttons from main menu!", ex);
        }
        return output;
    }
}
