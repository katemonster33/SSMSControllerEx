package ssms.controller.reflection;

import com.fs.graphics.util.Fader;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.title.TitleScreenState;
import com.fs.state.AppDriver;

import java.util.ArrayList;
import java.util.List;

public class TitleScreenStateReflector {
    MethodReflector getScreenPanel = null;
    FieldReflector dialogTypeField = null;
    FieldReflector missionWidgetField = null;
    Object cs;
    public TitleScreenStateReflector() {
        cs = AppDriver.getInstance().getState(TitleScreenState.STATE_ID);

        var csReflector = new ClassReflector(cs.getClass());
        getScreenPanel = csReflector.findDeclaredMethod("getScreenPanel");
        dialogTypeField = csReflector.getDeclaredField("dialogType");
        missionWidgetField = csReflector.getDeclaredField("missionWidget");

        if(getScreenPanel == null || dialogTypeField == null || missionWidgetField == null) {
            throw new RuntimeException("Couldn't reflect into Title Screen!");
        }
    }

    public UIPanelAPI getMissionWidget() {
        return (UIPanelAPI) missionWidgetField.get(cs);
    }

    public Object getDialogType() {
        return dialogTypeField.get(cs);
    }

    public UIPanelAPI getMainMenu() {
        var widgets = new UIPanelReflector(getScreenPanel()).getChildItems();
        var titleWidget = (UIPanelAPI) widgets.get(0);

        var getMainMenu = new ClassReflector(titleWidget.getClass()).findDeclaredMethod("getMainMenu");

        return (UIPanelAPI) getMainMenu.invoke(titleWidget);
    }

    public UIPanelAPI getScreenPanel() {
        return (UIPanelAPI) getScreenPanel.invoke(cs);
    }

    public Object getMainMenuMode() {
        var mainMenu = getMainMenu();
        var getMainMenuMode = new ClassReflector(mainMenu.getClass()).getDeclaredMethod("getMode");
        return getMainMenuMode.invoke(mainMenu);
    }

    public List<ButtonAPI> getMainMenuButtons() {
        List<ButtonAPI> output = new ArrayList<>();

        var mainMenuPanel = new UIPanelReflector(getMainMenu());
        var mainMenuWidgets = mainMenuPanel.getChildPanels();

        if (!mainMenuWidgets.isEmpty()) {
            output.addAll(new UIPanelReflector(mainMenuWidgets.get(0)).getChildButtons());
        }
        return output;
    }
}
