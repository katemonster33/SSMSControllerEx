package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

import java.util.ArrayList;
import java.util.List;

public class TitleScreenStateReflector {
    Object getScreenPanel = null;
    Object dialogTypeField = null;
    Object missionWidgetField = null;
    Object cs;
    static TitleScreenStateReflector instance;
    private TitleScreenStateReflector() {
        cs = AppDriver.getInstance().getState(TitleScreenState.STATE_ID);

        try {
            getScreenPanel = ClassReflector.GetInstance().findDeclaredMethod(TitleScreenState.class, "getScreenPanel");
            dialogTypeField = ClassReflector.GetInstance().getDeclaredField(TitleScreenState.class, "dialogType");
            missionWidgetField = ClassReflector.GetInstance().getDeclaredField(TitleScreenState.class, "missionWidget");
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

    public UIPanelAPI getMissionWidget() {
        try {
            return (UIPanelAPI) FieldReflector.GetInstance().GetVariable(missionWidgetField, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get mission widget!", ex);
        }
        return null;
    }

    public Fader getMissionWidgetFader() {
        try {
            var missionWidget = FieldReflector.GetInstance().GetVariable(missionWidgetField, cs);

            var getFaderMethod = ClassReflector.GetInstance().findDeclaredMethod(missionWidget.getClass(), "getFader");

            return (Fader) MethodReflector.GetInstance().invoke(getFaderMethod, missionWidget);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get mission widget fader!", ex);
        }
        return null;
    }

    public Object getDialogTypeField() {
        try {
            return FieldReflector.GetInstance().GetVariable(dialogTypeField, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get dialog type!", ex);
        }
        return null;
    }

    public UIPanelAPI getMainMenu() {
        var widgets = UIPanelReflector.getChildItems(getScreenPanel());
        var titleWidget = (UIPanelAPI) widgets.get(0);

        try {
            var getMainMenu = ClassReflector.GetInstance().findDeclaredMethod(titleWidget.getClass(), "getMainMenu");

            return (UIPanelAPI) MethodReflector.GetInstance().invoke(getMainMenu, titleWidget);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get main menu widget!", ex);
        }
        return null;
    }

    public UIPanelAPI getScreenPanel() {
        try {
            return (UIPanelAPI) MethodReflector.GetInstance().invoke(getScreenPanel, cs);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).warn("Couldn't call TitleScreenState.getScreenPanel!", ex);
            return null;
        }
    }

    public Object getMainMenuMode() {
        var mainMenu = getMainMenu();
        try {
            var getMainMenuMode = ClassReflector.GetInstance().getDeclaredMethod(mainMenu.getClass(), "getMode");
            return MethodReflector.GetInstance().invoke(getMainMenuMode, mainMenu);
        } catch(Throwable ex) {
            Global.getLogger(getClass()).error("Couldn't get mode of main menu!", ex);
        }
        return null;
    }

    public List<ButtonAPI> getMainMenuButtons() {
        List<ButtonAPI> output = new ArrayList<>();

        try {
            var mainMenuPanel = getMainMenu();
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
